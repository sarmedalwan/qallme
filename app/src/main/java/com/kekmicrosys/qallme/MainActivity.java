package com.kekmicrosys.qallme;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "QallMe_Tag";
    public AdminSQLiteOpenHelper admin;
    public SQLiteDatabase bd;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        admin = new AdminSQLiteOpenHelper(this, "kekmicrosysqallme.db", null, 1);
        bd = admin.getWritableDatabase();
    }

    @Override
    protected void onResume(){
        super.onResume();
        changeMessage();
        sendNfcMessage();
        //Log.d(LOG_TAG, String.valueOf(this));
    }

    private void sendNfcMessage(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter == null || !mNfcAdapter.isEnabled()){
            Toast.makeText(this, this.getString(R.string.msj_05), Toast.LENGTH_LONG).show();
            return;
        }

        mNfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback(){
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                try{
                    NdefMessage msg = createNdefMessageSend(event);
                    return msg;
                } catch (Exception e){
                    Log.d(LOG_TAG, String.valueOf(e));
                }

                return null;
            };
        }, this);
    }

    private NdefMessage createNdefMessageSend(NfcEvent event){
        Map<String, String> my_data = getMyData();
        String apellido = my_data.get("apellido");
        String nombre = my_data.get("nombre");
        String celnum = my_data.get("celnum");

        NdefMessage msg = new NdefMessage(
                new NdefRecord[] {
                        // mime_type definido para la app:
                        NdefRecord.createMime(getString(R.string.mime_type), apellido.getBytes()),
                        NdefRecord.createMime(getString(R.string.mime_type), nombre.getBytes()),
                        NdefRecord.createMime(getString(R.string.mime_type), celnum.getBytes()),

                        // Indico la app especifica que debe abrir (AAR):
                        NdefRecord.createApplicationRecord("com.kekmicrosys.qallme")
                });
        return msg;
    }

    private Map<String, String> getMyData(){
        Map<String, String> map_data = new HashMap<String, String>();
        map_data.put("apellido", "");
        map_data.put("nombre", "");
        map_data.put("celnum", "");

        Cursor fila = bd.rawQuery("SELECT * FROM usuario WHERE id = 1", null);
        if(fila.moveToFirst()){
            map_data.put("apellido", fila.getString(1));
            map_data.put("nombre", fila.getString(2));
            map_data.put("celnum", fila.getString(3));
        }

        return map_data;
    }

    private void changeMessage(){
        TextView txt_01 = (TextView)findViewById(R.id.txt_01);
        if(myDataExists()){
            txt_01.setText(this.getString(R.string.msj_00));
        } else {
            txt_01.setText(this.getString(R.string.msj_01));
        }
    }

    private boolean myDataExists(){
        AdminSQLiteOpenHelper admin;
        SQLiteDatabase bd;
        admin = new AdminSQLiteOpenHelper(this, "kekmicrosysqallme.db", null, 1);
        bd = admin.getWritableDatabase();
        Cursor fila = bd.rawQuery("SELECT * FROM usuario WHERE id = 1", null);
        if(fila.moveToFirst()){
            return true;
        } else {
            return false;
        }
    }

    public void listContacts(View view){
        Intent i = new Intent(this, ContactsActivity.class);
        startActivity(i);
    }

    public void frmMyData(View view){
        Intent i = new Intent(this, MyDataActivity.class);
        startActivity(i);
    }

    public void quitApp(View view){
        Log.d(LOG_TAG, "PRAISE KEK!");
        finish();
        System.exit(0);
    }
}
