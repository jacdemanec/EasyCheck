package net.marcarni.easycheck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import net.marcarni.easycheck.RecyclerView.Header;
import net.marcarni.easycheck.RecyclerView.HeaderAdapter;
import net.marcarni.easycheck.SQLite.ContracteBD.Reserves;
import net.marcarni.easycheck.SQLite.DBInterface;
import net.marcarni.easycheck.settings.MenuAppCompatActivity;

import java.util.ArrayList;

public class DetallActivity extends MenuAppCompatActivity {

    DBInterface db;
    private HeaderAdapter headerAdapter;
    ArrayList<Header> myDataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detall);

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        myDataset=new ArrayList<>();
        headerAdapter= new HeaderAdapter(myDataset);
        recyclerView.setAdapter(headerAdapter);
        db = new DBInterface(this);

        /*
         * Per un correcte funcionament cal llençar "CrearExemplesBD()" el primer cop que
         * s'instal·la l'app, a continuació comentar aquest mètode i tornar a instal·lar l'app
         */
        CrearExemplesBD();
        consultes();
        verifica();
    }
    public void verifica(){
        if (myDataset.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Reserva no trobada!")
                    .setTitle("Atenció!!")
                    .setCancelable(false)
                    .setPositiveButton("Acceptar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    finish();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

        }
    }
    public void consultes(){
        if (getIntent().hasExtra("LOCALITZADOR")){
            String loc = getIntent().getExtras().getString("LOCALITZADOR");
            RetornaReservaLoc(loc);
        } else if (getIntent().hasExtra(getString(R.string.scanner_result))) {
            String qrCode = getIntent().getStringExtra(getString(R.string.scanner_result));
            RetornaReservaQR(qrCode);
        } else if(getIntent().hasExtra("DNI") && getIntent().hasExtra("DATA")) {
            String dni = getIntent().getExtras().getString("DNI");
            String data = getIntent().getExtras().getString("DATA");
            RetornaReservaDNI_DATA(dni, data);
        } else if (getIntent().hasExtra("DATA")) {
            String data = getIntent().getExtras().getString("DATA");
            RetornaReservaDATA(data);
        } else if (getIntent().hasExtra("DNI")){
            String dni = getIntent().getExtras().getString("DNI");
            RetornaReservaDNI(dni);
        } else if (getIntent().hasExtra("ID_SERVEI")){
            String id_reserva = getIntent().getExtras().getString("ID_SERVEI");
            RetornaReservaId_Reserva(id_reserva);
        }else {
            //NO S'HAURÍA D'ARRIBAR A AQUEST PUNT!!!
            Toast.makeText(this, "No s'ah rebut cap criteri de cerca", Toast.LENGTH_LONG).show();
        }
    }
    public void RetornaReservaId_Reserva(String id_reserva){
        db.obre();
        Cursor cursor=db.RetornaReservaId_Reserva(id_reserva);
        CursorBD(cursor);
        db.tanca();
    }
    public void RetornaReservaDNI(String dni) {
        db.obre();
        Cursor cursor=db.RetornaReservaDNI(dni);
        CursorBD(cursor);
        db.tanca();
    }

    public void RetornaReservaDNI_DATA(String dni,String data){

        db.obre();
        Cursor cursor=db.RetornaReservaDNI_DATA(dni,data);
        CursorBD(cursor);
        db.tanca();
    }
    public void RetornaReservaDATA(String data){

        db.obre();
        Cursor cursor=db.RetornaReservaData(data);
        CursorBD(cursor);
        db.tanca();
    }
    private void RetornaReservaLoc(String loc) {
        Toast.makeText(getBaseContext(), loc, Toast.LENGTH_SHORT).show();
        db.obre();
        Cursor cursor=db.RetornaReservaLocalitzador(loc);
        CursorBD(cursor);
        db.tanca();
    }

    public void RetornaReserva(String dni, String data){

        Cursor cursor=db.RetornaTotesLesReserves();
        CursorBD(cursor);
        db.tanca();
    }

    public void RetornaReservaQR(String qrCode){
        db.obre();
        Cursor cursor=db.RetornaReservaQR(qrCode);
        CursorBD(cursor);
        db.tanca();
    }


    public void CursorBD(Cursor cursor){
        if(cursor.moveToFirst()){
            do {
                myDataset.add(new Header(cursor.getString(cursor.getColumnIndex(Reserves.NOMBRE_TITULAR))+" "+cursor.getString(cursor.getColumnIndex(Reserves.APELLIDO1_TITULAR))+" "+cursor.getString(cursor.getColumnIndex(Reserves.APELLIDO2_TITULAR)),
                        "DNI: "+cursor.getString(cursor.getColumnIndex(Reserves.DNI_TITULAR))+"  "+"Data Servei: " +cursor.getString(cursor.getColumnIndex(Reserves.FECHA_SERVICIO)),
                        "QR: "+cursor.getString(cursor.getColumnIndex(Reserves.QR_CODE))+"   Localització: "+cursor.getString(cursor.getColumnIndex(Reserves.LOCALIZADOR))
                        , cursor.getString(cursor.getColumnIndex(Reserves.EMAIL_TITULAR)),cursor.getString(cursor.getColumnIndex(Reserves.CHECK_IN))));

            }while(cursor.moveToNext());

        }
    }

    /*
 * Crea els exemples per tal de provar l'app, l'ordre és important:
 * primer treballador, després servei i per últim reserva, per tal de
 * no crear conflictes amb les claus primaries.
 */
    public void CrearExemplesBD(){
        db.obre();
        db.Esborra();

        db.InserirTreballador("Toni","Torres","Mari","jacdemanec","xxx","1");
        db.InserirTreballador("Maria","Ortega","Cobo","mari","xxx","1");
        db.InserirTreballador("Carlos Alberto","Castro","Cañabate","carlos","xxx","1");

        db.InserirServei("Tarragona - Reus", "3");

        db.InserirServei("Barcelona - Reus", "2");
        db.InserirServei("Barcelona - Seu d'urgell", "3");
        db.InserirServei("Eivissa - Formentera", "1");
        db.InserirServei("Eivissa - Formentera", "2");
        db.InserirServei("Barcelona - Seu d'urgell", "1");
        db.InserirServei("Sabadell - Girona", "3");

                         //LOC   DATA reserva data servei idservei                                                                         QR            DNI
        db.InserirReserva("123456","16/1/2017","29/10/2017",1,"Maria","Ortega","Cobos","12345678","maria@gmail.com","45R545WE45","0","41471860P");
        db.InserirReserva("123446","16/3/2017","19/11/2017",1,"Joana","Fidel","Sanchis","12345998","jaoan@gmail.com","45R545WE45","0","38039532Q");
        db.InserirReserva("55546","3/3/2017","3/1/2018",2,"Pere","Fernandez","Pujol","22342998","perean@gmail.com","854HFHH945","0","99392359K");
        db.InserirReserva("54446","10/3/2017","23/2/2018",2,"Pamela","Sanchez","Grau","14445998","raimn@gmail.com","66FHHF45","0","72339884P");
        db.InserirReserva("5746","9/3/2017","31/1/2018",3,"Antoni","Puig","Puigdemont","12356998"," olmean@gmail.com","867FHH9945","0","20841817E");
        db.InserirReserva("55666","10/3/2017","19/1/2018",3,"Enma","Smith","Delon","16789998","iuoean@gmail.com","ABCDE","0","97620922K");
        db.InserirReserva("55669","10/3/2017","19/11/2017",3,"Maria","Smith","Delon","16789998","iuoean@gmail.com","ABCDE","0","17370830B");
        db.InserirReserva("556665","10/3/2017","19/11/2017",7,"Nuria","Smith","Delon","16789998","iuoean@gmail.com","ABCDE","0","47169530A");
        db.tanca();
    }
}
