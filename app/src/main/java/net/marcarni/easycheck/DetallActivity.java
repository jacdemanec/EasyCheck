package net.marcarni.easycheck;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.marcarni.easycheck.RecyclerView.Header;
import net.marcarni.easycheck.RecyclerView.HeaderAdapter;
import net.marcarni.easycheck.SQLite.ContracteBD;
import net.marcarni.easycheck.SQLite.ContracteBD.Reserves;
import net.marcarni.easycheck.SQLite.ContracteBD.Serveis;
import net.marcarni.easycheck.SQLite.DBInterface;
import net.marcarni.easycheck.Utils.NetUtils;
import net.marcarni.easycheck.model.Reserva;
import net.marcarni.easycheck.model.Servei;
import net.marcarni.easycheck.settings.MenuAppCompatActivity;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetallActivity extends MenuAppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Header>> {

    private DBInterface db;
    private HeaderAdapter headerAdapter;
    private ArrayList<Header> myDataset;
    private String mHost;
    private int mPort;
    private static final String PATH = "/easycheckapi/servei";
    private static final int EASY_LOADER = 22;

    /**
     * @param savedInstanceState Bundle
     * @author Carlos Alberto Castro Cañabate
     * Mètode onCreate de DetallActivity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detall);

        //@Author Toni Torres
        //Agafa el host i el port de les preferencies d'usuari
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mHost = sharedPreferences.getString(getString(R.string.pref_host_key), getString(R.string.pref_host_default));
        mPort = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_port_key), getString(R.string.pref_port_default)));

        /**
         * created by Maria Remedios Ortega Cobos
         * instanciació i inicialització del Recycler, adaptador i
         * ArrayList
         */
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        myDataset = new ArrayList<>();
        headerAdapter = new HeaderAdapter(myDataset);
        recyclerView.setAdapter(headerAdapter);

        if (NetUtils.comprovaXarxa(this)) {
            consultesOnline();
        } else {
            db = new DBInterface(this);
            consultes();
            verifica(this);
        }

    }

    /**
     * @author Carlos Alberto Castro Cañabate
     * Mètode que verigica si el dataSet es buit, es a dir sino hi ha reserva
     */
    public void verifica(final Context context) {
        if (myDataset.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Reserva no trobada!")
                    .setTitle("Atenció!!")
                    .setCancelable(false)
                    .setPositiveButton("Acceptar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    if (getIntent().hasExtra(getString(R.string.scanner_result))) {
                                        Intent intent = new Intent(context, CheckCameraPermissionsActivity.class);
                                        startActivity(intent);
                                    }
                                    finish();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * @author Carlos Alberto Castro Cañabate
     * Mètode per obtenir les dades de l'intent.
     */
    public void consultes() {
        if (getIntent().hasExtra("LOCALITZADOR")) {
            String loc = getIntent().getExtras().getString("LOCALITZADOR");
            RetornaReservaLoc(loc);
        } else if (getIntent().hasExtra(getString(R.string.scanner_result))) {
            String qrCode = getIntent().getStringExtra(getString(R.string.scanner_result));
            RetornaReservaQR(qrCode);
        } else if (getIntent().hasExtra("DNI") && getIntent().hasExtra("DATA")) {
            String dni = getIntent().getExtras().getString("DNI");
            String data = getIntent().getExtras().getString("DATA");
            RetornaReservaDNI_DATA(dni, data);
        } else if (getIntent().hasExtra("DATA")) {
            String data = getIntent().getExtras().getString("DATA");
            RetornaReservaDATA(data);
        } else if (getIntent().hasExtra("DNI")) {
            String dni = getIntent().getExtras().getString("DNI");
            RetornaReservaDNI(dni);
        } else if (getIntent().hasExtra("ID_SERVEI")) {
            String id_reserva = getIntent().getExtras().getString("ID_SERVEI");
            RetornaReservaId_Reserva(id_reserva);
        } else {
            Toast.makeText(this, "No s'ah rebut cap criteri de cerca", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @param id_reserva a filtrar
     * @author Carlos Alberto Castro Cañabate
     * Mètode per obtenir les reserves filtrades per id
     */
    public void RetornaReservaId_Reserva(String id_reserva) {
        db.obre();
        Cursor cursor = db.RetornaReservaId_Servei(id_reserva);
        CursorBD(cursor);
        db.tanca();
    }

    /**
     * @param dni a filtrar
     * @author Carlos Alberto Castro Cañabate
     * Mètode per obtenir les reserves filtrades per dni
     */
    public void RetornaReservaDNI(String dni) {
        db.obre();
        Cursor cursor = db.RetornaReservaDNI(dni);
        CursorBD(cursor);
        db.tanca();
    }

    /**
     * @param dni  a filtrar
     * @param data a fitrar
     * @author Carlos Alberto Castro Cañabate
     * Mètode per obtenir les reserves filtrades per dni i data
     */
    public void RetornaReservaDNI_DATA(String dni, String data) {
        db.obre();
        Cursor cursor = db.RetornaReservaDNI_DATA(dni, data);
        CursorBD(cursor);
        db.tanca();
    }

    /**
     * @param data a filtrar
     * @author Carlos Alberto Castro Cañabate
     * Mètode per obtenir les reserves filtrades per data
     */
    public void RetornaReservaDATA(String data) {
        db.obre();
        Cursor cursor = db.RetornaReservaData(data);
        CursorBD(cursor);
        db.tanca();
    }

    /**
     * @param loc localitzador a filtrar
     * @author Carlos Alberto Castro Cañabate
     * Mètode per obtenir les reserves filtrades per localitzador
     */
    private void RetornaReservaLoc(String loc) {
        //Toast.makeText(getBaseContext(), loc, Toast.LENGTH_SHORT).show();
        db.obre();
        Cursor cursor = db.RetornaReservaLocalitzador(loc);
        CursorBD(cursor);
        db.tanca();
    }

    /**
     * @param qrCode a filtrar
     * @author Carlos Alberto Castro Cañabate
     * Mètode per obtenir les reserves filtrades per QR
     */
    public void RetornaReservaQR(String qrCode) {
        db.obre();
        Cursor cursor = db.RetornaReservaQR(qrCode);
        CursorBD(cursor);
        db.tanca();
    }

    /**
     * @param cursor a afegir entrada
     * @author Carlos Alberto Castro Cañabate
     * Mètode per afegir una nova entrada al dataSet
     */
    public void CursorBD(Cursor cursor) {
        if (cursor.moveToFirst()) {
            do {
                myDataset.add(new Header(cursor.getInt(cursor.getColumnIndex(Reserves._ID)), cursor.getString(cursor.getColumnIndex(ContracteBD.Client.NOM_TITULAR)) + " " + cursor.getString(cursor.getColumnIndex(ContracteBD.Client.COGNOM1_TITULAR)) + " " + cursor.getString(cursor.getColumnIndex(ContracteBD.Client.COGNOM2_TITULAR)),
                        "DNI: " + cursor.getString(cursor.getColumnIndex(ContracteBD.Client.DNI_TITULAR)), "Data Servei: " + cursor.getString(cursor.getColumnIndex(Serveis.DATA_SERVEI)),
                        "QR: " + cursor.getString(cursor.getColumnIndex(Reserves.QR_CODE)), "Localització: " + cursor.getString(cursor.getColumnIndex(Reserves.LOCALITZADOR))
                        , cursor.getString(cursor.getColumnIndex(ContracteBD.Client.EMAIL_TITULAR)), cursor.getString(cursor.getColumnIndex(Reserves.CHECK_IN)), cursor.getString(cursor.getColumnIndex(Serveis.DESCRIPCIO))));

            } while (cursor.moveToNext());
        }
    }

    /**
     * @Author Antoni Torres Marí
     * Mètode que obtè les dades del intent i les passa al Loader encarregat de
     * obtindre les dades del servidor
     */
    public void consultesOnline() {
        Bundle queryBundle = new Bundle();
        if (getIntent().hasExtra("LOCALITZADOR")) {
            String loc = getIntent().getExtras().getString("LOCALITZADOR");
            queryBundle.putString("loc", loc);
        } else if (getIntent().hasExtra(getString(R.string.scanner_result))) {
            String qrCode = getIntent().getStringExtra(getString(R.string.scanner_result));
            queryBundle.putString("qrcode", qrCode);
        } else if (getIntent().hasExtra("DNI") && getIntent().hasExtra("DATA")) {
            String dni = getIntent().getExtras().getString("DNI");
            String data = getIntent().getExtras().getString("DATA");
            queryBundle.putString("dni", dni);
            queryBundle.putString("data", data);
        } else if (getIntent().hasExtra("DATA")) {
            String data = getIntent().getExtras().getString("DATA");
            queryBundle.putString("data", data);
        } else if (getIntent().hasExtra("DNI")) {
            String dni = getIntent().getExtras().getString("DNI");
            queryBundle.putString("dni", dni);
        } else if (getIntent().hasExtra("ID_SERVEI")) {
            String id_servei = getIntent().getExtras().getString("ID_SERVEI");
            queryBundle.putString("idservei", id_servei);
        } else {
            Toast.makeText(this, "No s'ah rebut cap criteri de cerca", Toast.LENGTH_LONG).show();
        }
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> easySearchLoader = loaderManager.getLoader(EASY_LOADER);
        if (easySearchLoader == null) {
            loaderManager.initLoader(EASY_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(EASY_LOADER, queryBundle, this);
        }
    }

    /**
     * @Author Antoni Torres Marí
     *
     */
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<ArrayList<Header>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<Header>>(this) {

            ArrayList<Header> dataSet;
            Gson gson;

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }

                gson = new Gson();
                //Mostra Loading Indicator
                if (dataSet != null) {
                    deliverResult(dataSet);
                } else {
                    forceLoad();
                }

            }


            @Override
            public ArrayList<Header> loadInBackground() {
                URL url = NetUtils.buildUrl(mHost, mPort, PATH, null);
                String json = NetUtils.doGetRequest(url);
                Type tipusLlistaDeServeis = new TypeToken<List<Servei>>() {
                }.getType();
                List<Servei> llistaServeis = gson.fromJson(json, tipusLlistaDeServeis);
                ArrayList<Header> data = new ArrayList<>();
                if (args.containsKey("loc")) {
                    for (Servei servei : llistaServeis) {
                        for (Reserva reserva : servei.getLlistaReserves()) {
                            if (reserva.getLocalitzador().equals(args.get("loc"))) {
                                data.add(new Header(reserva.getId(), reserva.getClient().getNom_titular() +
                                        " " + reserva.getClient().getCognom1_titular() + " " + reserva.getClient().getCognom2_titular(),
                                        reserva.getClient().getDni_titular(), servei.getData_servei(), reserva.getQr_code(), reserva.getLocalitzador(),
                                        reserva.getClient().getEmail_titular(), "" + reserva.getCheckin(), servei.getDescripcio()));
                            }
                        }
                    }
                } else if (args.containsKey("dni") && args.containsKey("data")) {
                    for (Servei servei : llistaServeis) {
                        for (Reserva reserva : servei.getLlistaReserves()) {
                            if (reserva.getClient().getDni_titular().equals(args.get("dni")) && servei.getData_servei().equals(args.get("data"))) {
                                data.add(new Header(reserva.getId(), reserva.getClient().getNom_titular() +
                                        " " + reserva.getClient().getCognom1_titular() + " " + reserva.getClient().getCognom2_titular(),
                                        reserva.getClient().getDni_titular(), servei.getData_servei(), reserva.getQr_code(), reserva.getLocalitzador(),
                                        reserva.getClient().getEmail_titular(), "" + reserva.getCheckin(), servei.getDescripcio()));
                            }
                        }
                    }
                } else if (args.containsKey("dni")) {
                    for (Servei servei : llistaServeis) {
                        for (Reserva reserva : servei.getLlistaReserves()) {
                            if (reserva.getClient().getDni_titular().equals(args.get("dni"))) {
                                data.add(new Header(reserva.getId(), reserva.getClient().getNom_titular() +
                                        " " + reserva.getClient().getCognom1_titular() + " " + reserva.getClient().getCognom2_titular(),
                                        reserva.getClient().getDni_titular(), servei.getData_servei(), reserva.getQr_code(), reserva.getLocalitzador(),
                                        reserva.getClient().getEmail_titular(), "" + reserva.getCheckin(), servei.getDescripcio()));
                            }
                        }
                    }
                } else if (args.containsKey("data")) {
                    for (Servei servei : llistaServeis) {
                        if (servei.getData_servei().equals(args.get("data"))) {
                            for (Reserva reserva : servei.getLlistaReserves()) {
                                data.add(new Header(reserva.getId(), reserva.getClient().getNom_titular() +
                                        " " + reserva.getClient().getCognom1_titular() + " " + reserva.getClient().getCognom2_titular(),
                                        reserva.getClient().getDni_titular(), servei.getData_servei(), reserva.getQr_code(), reserva.getLocalitzador(),
                                        reserva.getClient().getEmail_titular(), "" + reserva.getCheckin(), servei.getDescripcio()));
                            }
                        }
                    }
                } else if (args.containsKey("qrcode")) {
                    for (Servei servei : llistaServeis) {
                        for (Reserva reserva : servei.getLlistaReserves()) {
                            if (reserva.getQr_code().equals(args.get("qrcode"))) {
                                data.add(new Header(reserva.getId(), reserva.getClient().getNom_titular() +
                                        " " + reserva.getClient().getCognom1_titular() + " " + reserva.getClient().getCognom2_titular(),
                                        reserva.getClient().getDni_titular(), servei.getData_servei(), reserva.getQr_code(), reserva.getLocalitzador(),
                                        reserva.getClient().getEmail_titular(), "" + reserva.getCheckin(), servei.getDescripcio()));
                            }
                        }
                    }
                } else if (args.containsKey("idservei")) {
                    for (Servei servei : llistaServeis) {
                        if (args.get("idservei").equals(""+servei.getId())) {
                            for (Reserva reserva : servei.getLlistaReserves()) {
                                data.add(new Header(reserva.getId(), reserva.getClient().getNom_titular() +
                                        " " + reserva.getClient().getCognom1_titular() + " " + reserva.getClient().getCognom2_titular(),
                                        reserva.getClient().getDni_titular(), servei.getData_servei(), reserva.getQr_code(), reserva.getLocalitzador(),
                                        reserva.getClient().getEmail_titular(), "" + reserva.getCheckin(), servei.getDescripcio()));
                            }
                        }
                    }
                }
                return data;
            }

            @Override
            public void deliverResult(ArrayList<Header> dataset) {
                dataSet = dataset;
                super.deliverResult(dataset);
            }
        };
    }

    /**
     * @Author Antoni Torres Marí
     *
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<Header>> loader, ArrayList<Header> data) {
        if (data.isEmpty()) {
            verifica(DetallActivity.this);
        } else {
            myDataset = data;
            headerAdapter.actualitzaRecycler(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Header>> loader) {

    }

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String queryUrl = mUrlDisplayTextView.getText().toString();
        outState.putString(SEARCH_QUERY_URL_EXTRA, queryUrl);
    }*/
}
