package com.kekmicrosys.qallme;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReceiverActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "QallMe_PraiseKek";
    GoogleMap mapa;
    public NdefRecord[] records;
    public Intent intent;
    public NfcAdapter mNfcAdapter;
    private EditText txt_ctoape, txt_ctonom, txt_ctocel;
    private AdminSQLiteOpenHelper admin;
    private SQLiteDatabase bd;
    private String lat;
    private String lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        admin = new AdminSQLiteOpenHelper(this, "kekmicrosysqallme.db", null, 1);
        bd = admin.getWritableDatabase();

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        intent = getIntent();
        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessage(null, this);
        }

        txt_ctoape = (EditText)findViewById(R.id.txt_cto_ape);
        txt_ctonom = (EditText)findViewById(R.id.txt_cto_nom);
        txt_ctocel = (EditText)findViewById(R.id.txt_cto_cel);

        txt_ctoape.setFocusable(false); txt_ctoape.setClickable(false);
        txt_ctonom.setFocusable(false); txt_ctonom.setClickable(false);
        txt_ctocel.setFocusable(false); txt_ctocel.setClickable(false);

        setGMap();
    }

    @Override
    protected void onResume(){
        super.onResume();
        showReceivedData();
    }

    private void saveContact(String apellido, String nombre, String celnum){
        SQLiteStatement stc = bd.compileStatement("INSERT INTO contactos " +
                "(apellido,nombre,celnum,latitud,longitud) VALUES (?, ?, ?, ?, ?)");
        stc.bindString(1, apellido);
        stc.bindString(2, nombre);
        stc.bindString(3, celnum);
        stc.bindString(4, lat);
        stc.bindString(5, lon);

        stc.execute();
        stc.close();
        bd.close();

        backMainActivity();
    }

    public void addContact(View v){
        String apellido = txt_ctoape.getText().toString();
        String nombre = txt_ctonom.getText().toString();
        String celnum = txt_ctocel.getText().toString().trim();
        String display_name = nombre+" "+apellido;

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int raw_cto_id = 0;

        // Primero agrego un "raw contact":
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.
                newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,  raw_cto_id)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, display_name)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, apellido)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, nombre)
                .build());

        // Agregamos el numero:
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, raw_cto_id)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, celnum)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        // Ejecutamos la sentencia:
        boolean resul = true;
        try{
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            saveContact(apellido, nombre, celnum);
        }catch (Exception e) {
            resul = false;
            Log.d(LOG_TAG, String.valueOf(e));
        }

        if(resul){
            Toast.makeText(getBaseContext(), this.getString(R.string.msj_06), Toast.LENGTH_SHORT).show();
            backMainActivity();
        } else {
            Toast.makeText(getBaseContext(), this.getString(R.string.msj_07), Toast.LENGTH_SHORT).show();
        }
    }

    private void showReceivedData(){
        NdefMessage[] msgs = getNfcMessages();
        if(msgs == null){
            Log.d(LOG_TAG, "No NFC messages (PRAISE KEK!).");
            return;
        }

        records = msgs[0].getRecords();
        String apellido = new String(records[0].getPayload());
        String nombre = new String(records[1].getPayload());
        String celnum = new String(records[2].getPayload());

        txt_ctoape.setText(apellido);
        txt_ctonom.setText(nombre);
        txt_ctocel.setText(celnum);

        //Log.d(LOG_TAG, String.valueOf(this));
    }

    private NdefMessage[] getNfcMessages(){
        NdefMessage[] msgs = null;
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            // Obtenemos los mensajes:
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
        }

        return msgs;
    }

    public void rejectContact(View v){
        backMainActivity();
    }

    private void backMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        finish();
        startActivity(i);
    }

    private void setGMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map<String, String> map_coords = getCoords();
        lat = map_coords.get("latitud");
        lon = map_coords.get("longitud");
        double latitud = Double.parseDouble(lat);
        double longitud = Double.parseDouble(lon);

        mapa = googleMap;
        // 51.48257659999999, -0.007658900000024005
        mapa.setMapType(googleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);
        LatLng ciudad = new LatLng(latitud, longitud);
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(ciudad, 18));
        MarkerOptions marcador = new MarkerOptions().title("^").position(ciudad);
        mapa.addMarker(marcador);
    }

    public Map<String, String> getCoords() {
        Map<String, String> map_coords = new HashMap<String, String>();
        map_coords.put("latitud", "0");
        map_coords.put("longitud", "0");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "ReceiverActivity.getCoords: No internet conection");
            return map_coords;
        }

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        double latitud = location.getLatitude();
        double longitud = location.getLongitude();
        map_coords.put("latitud", String.valueOf(latitud));
        map_coords.put("longitud", String.valueOf(longitud));
        return map_coords;
    }
}
