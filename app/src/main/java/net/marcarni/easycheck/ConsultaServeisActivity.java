package net.marcarni.easycheck;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import net.marcarni.easycheck.RecyclerView.HeaderAdapter_Consulta;
import net.marcarni.easycheck.RecyclerView.Header_Consulta;
import net.marcarni.easycheck.SQLite.DBInterface;
import net.marcarni.easycheck.settings.MenuAppCompatActivity;

import java.util.ArrayList;

public class ConsultaServeisActivity extends MenuAppCompatActivity {

    private static final int DATE_PICKER_REQUEST = 22;
    DBInterface db;
    private HeaderAdapter_Consulta headerAdapter_consulta;
    ArrayList<Header_Consulta> myDataset;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_serveis);
        myDataset = new ArrayList<>();
        headerAdapter_consulta = new HeaderAdapter_Consulta(myDataset);
        db=new DBInterface(this);
        db.obre();
        //Configuració del toolbar amb els filtres
        Toolbar editToolbar = (Toolbar) findViewById(R.id.filter_toolbar);
        editToolbar.inflateMenu(R.menu.toolbar_menu);
        Spinner spinnerTreballadors = (Spinner) findViewById(R.id.spinner_de_treballadors);
        // Afegeixo Recycler per instanciar
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_consulta);

        //TODO 1: Cursor amb dades test, s'ha d'esborrar el métode getFakeCursor y extraure-les de la bbdd

        Cursor cursorTest = db.RetornaTotsElsTreballadors();
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                cursorTest,
                new String[]{"nom"}, //Columna del cursor que volem agafar
                new int[]{android.R.id.text1}, 0);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTreballadors.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ((ActionMenuItemView) findViewById(R.id.seleccionar_data)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConsultaServeisActivity.this, CalendarActivity.class);
                startActivityForResult(intent, DATE_PICKER_REQUEST);
            }
        });
        if (spinnerTreballadors != null) {
            spinnerTreballadors.setAdapter(adapter);
            spinnerTreballadors.setOnItemSelectedListener(new myOnItemSelectedListener());
        }

        // Afegim Recycler
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(headerAdapter_consulta);
        if (getIntent().hasExtra("ID_TREBALLADOR")) {
            long id = getIntent().getExtras().getLong("ID_TREBALLADOR");
            RetornaServeidelTreballador((int)id);
        }
        RetornaServeidelTreballador(3);

        // RetornaServeis();
        db.tanca();
    }



    public void CursorBD(Cursor cursor) {
        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst()) {
                do {

                    myDataset.add(new Header_Consulta(cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2),
                            "Servei:    "+ cursor.getString(3),cursor.getString(4)));

                } while (cursor.moveToNext());

            }
        }}
    public void RetornaServeis() {
        db.obre();
        Cursor cursor = db.RetornaTotsElsServeis();
        CursorBD(cursor);
        db.tanca();
    }
    public void RetornaServeidelTreballador(int id){
        db.obre();
        Cursor cursor=db.RetornaServei_Treballador(id);
        if (cursor.moveToFirst()) {
            do {
                myDataset.add(new Header_Consulta(cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2),
                        "Servei:    "+ cursor.getString(3)
                        ,cursor.getString(4)));

            } while (cursor.moveToNext());
        }
        db.tanca();

    }


    /**
     * Recull el resultat de CalendarActivity
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == DATE_PICKER_REQUEST)
        {
            if(resultCode == RESULT_OK)
            {
                //TODO 2: Recollir la data seleccionada a l'activitat Calendar i actualitzar el recycler aquí
                String data = intent.getStringExtra("DATA");
                Log.d("Data seleccionada: ", data);
                Toast.makeText(this, "Data seleccionada: "+data, Toast.LENGTH_SHORT ).show();
                // fechaServicio

            }
        }
    }

}

/**
 * Listener per els items seleccionats al Spinner amb nombs de treballador
 */
class myOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
    private HeaderAdapter_Consulta headerAdapter_consulta;
    ArrayList<Header_Consulta> myDataset;
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        //TODO 3: Recarregar aquí el RecyclerView segons el treballador seleccionat (el paràmetre id correspón a la columna _id de treballadors)
        Toast.makeText(view.getContext(), "Treballador amb _ID = " + id + " seleccionat.", Toast.LENGTH_SHORT ).show();
       /* Context context = adapterView.getContext();
        Intent intent = new Intent (context,ConsultaServeisActivity.class);
        DBInterface db = new DBInterface(view.getContext());
        intent.putExtra("ID_TREBALLADOR",id);
        context.startActivity(intent);

        headerAdapter_consulta.actualitzaRecycler(myDataset);  */
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
