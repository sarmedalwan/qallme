package com.kekmicrosys.qallme;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ContactsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "QallMe_PraiseKek";
    private AdminSQLiteOpenHelper admin;
    private SQLiteDatabase bd;
    private ListView lv_contactos;
    public String idcontacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        admin = new AdminSQLiteOpenHelper(this, "kekmicrosysqallme.db", null, 1);
        bd = admin.getWritableDatabase();
        lv_contactos =(ListView)findViewById(R.id.lvcontacts);

        generateListView();
    }

    @Override
    public void onResume(){
        super.onResume();
        generateListView();
    }

    private void generateListView(){
        final ArrayList<HashMap<String, String>> arr_datos = getContacts();

        ArrayList<String> arr_texto = new ArrayList<String>();
        final ArrayList<String> arr_id = new ArrayList<String>();
        for(HashMap<String, String> datos : arr_datos){
            String id = datos.get("id");
            String nombre = datos.get("nombre");
            arr_id.add(id);
            arr_texto.add(nombre);
            //Log.d(LOG_TAG, datos.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arr_texto);

        //  ArrayAdapter<HashMap<String, String>> adapter = new ArrayAdapter<HashMap<String, String>>(this,android.R.layout.simple_list_item_1, arr_datos);

        lv_contactos.setAdapter(adapter);

        lv_contactos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                idcontacto = arr_id.get(i);
                frmMyContact(view);
                //Log.d(LOG_TAG, arr_id.get(i));
            }
        });

        //Log.d(LOG_TAG, Arrays.toString(arr_datos.toArray()));
    }

    private ArrayList<HashMap<String, String>> getContacts(){
        ArrayList<HashMap<String, String>> arr_datos = new ArrayList<HashMap<String, String>>();
        Cursor fila = bd.rawQuery("SELECT * FROM contactos ", null);
        while(fila.moveToNext()){
            int id = fila.getInt(0);
            String apellido = fila.getString(1);
            String nombre = fila.getString(2);

            Map<String, String> map_data = new HashMap<String, String>();
            map_data.put("id", String.valueOf(id));
            map_data.put("nombre", nombre+" "+apellido);
            arr_datos.add((HashMap<String, String>) map_data);
        }

        fila.close();

        return arr_datos;
    }

    public void frmMyContact (View v) {
        Intent i = new Intent(this, MyContactActivity.class);
        i.putExtra("idcontacto", idcontacto);
        startActivity(i);
    }

    public void btnBack(View v){
        bd.close();
        finish();
    }
}
