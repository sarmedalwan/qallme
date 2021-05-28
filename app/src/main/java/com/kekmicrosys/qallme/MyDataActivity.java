package com.kekmicrosys.qallme;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MyDataActivity extends AppCompatActivity {
    public static final String LOG_TAG = "QallMe_PraiseKek";
    private AdminSQLiteOpenHelper admin;
    private SQLiteDatabase bd;
    private EditText txt_apellido, txt_nombre, txt_celnum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_data);
        //dbFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"PhotoApp");

        admin = new AdminSQLiteOpenHelper(this, "kekmicrosysqallme.db", null, 1);
        bd = admin.getWritableDatabase();

        txt_apellido = (EditText)findViewById(R.id.txt_apellido);
        txt_nombre = (EditText)findViewById(R.id.txt_nombre);
        txt_celnum = (EditText)findViewById(R.id.txt_celnum);

        //Log.d(LOG_TAG, String.valueOf(this));

        getMyData();
    }

    private void getMyData(){
        Cursor fila = bd.rawQuery("SELECT * FROM usuario WHERE id = 1", null);
        if(fila.moveToFirst()){
            txt_apellido.setText(fila.getString(1));
            txt_nombre.setText(fila.getString(2));
            txt_celnum.setText(fila.getString(3));
        }
    }

    private List<String> validateData(String apellido, String nombre, String celnum){
        List<String> alertas = new ArrayList<String>();

        if(apellido.length() < 1){
            alertas.add(this.getString(R.string.msj_02));
        }

        if(nombre.length() < 1){
            alertas.add(this.getString(R.string.msj_03));
        }

        if(celnum.length() < 1){
            alertas.add(this.getString(R.string.msj_04));
        }

        if(alertas.size() == 0){
            return alertas;
        }

        for(String mensaje : alertas){
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }

        return alertas;
    }

    public void saveMyData(View v){
        String apellido = firstUpper(txt_apellido.getText().toString());
        String nombre = firstUpper(txt_nombre.getText().toString());
        String celnum = txt_celnum.getText().toString().trim();

        List<String> alertas = validateData(apellido, nombre, celnum);
        if(alertas.size() > 0){
            return;
        }

        SQLiteStatement stc;

        if(dataExists()){
            stc = bd.compileStatement("UPDATE usuario SET apellido = ?, nombre = ?, celnum= ? WHERE id = 1 ");
        } else {
            stc = bd.compileStatement("INSERT INTO usuario (apellido,nombre,celnum) VALUES (?, ?, ?)");
        }

        stc.bindString(1, apellido);
        stc.bindString(2, nombre);
        stc.bindString(3, celnum);
        stc.execute();
        stc.close();
        bd.close();
        finish();
    }

    private String firstUpper(String cadena){
        cadena = cadena.trim();
        if(cadena.length() == 0){
            return "";
        }
        return cadena.substring(0, 1).toUpperCase() + cadena.substring(1);
    }

    private boolean dataExists(){
        Cursor fila = bd.rawQuery("SELECT * FROM usuario WHERE id = 1", null);
        if (fila.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public void btnBack(View v){
        bd.close();
        finish();
    }
}
