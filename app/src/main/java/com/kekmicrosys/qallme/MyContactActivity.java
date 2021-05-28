package com.kekmicrosys.qallme;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MyContactActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "QallMe_PraiseKek";
    GoogleMap mapa2;
    private AdminSQLiteOpenHelper admin;
    private SQLiteDatabase bd;
    private double lat;
    private double lon;
    TextView tv_nombre;
    TextView tv_celnum;
    String idcontacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_contact);
        admin = new AdminSQLiteOpenHelper(this, "kekmicrosysqallme.db", null, 1);
        bd = admin.getWritableDatabase();

        Bundle bundle = getIntent().getExtras();
        idcontacto = bundle.getString("idcontacto");

        //Log.d(LOG_TAG, idcontacto);

        tv_nombre = (TextView)findViewById(R.id.tv_nombre);
        tv_celnum = (TextView)findViewById(R.id.tv_celnum);

        setData(idcontacto);
    }

    private void confirm(){

        AlertDialog alerta;
        alerta = new AlertDialog.Builder(this).create();
        alerta.setTitle(this.getString(R.string.titconfirmar));
        alerta.setMessage(this.getString(R.string.msj_08));
        String lbl_btnno = this.getString(R.string.btnno);
        String lbl_btnsi = this.getString(R.string.btnsi);

        alerta.setButton(DialogInterface.BUTTON_NEGATIVE, lbl_btnsi, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int wich) {
                deleteContact();
            }
        });

        alerta.setButton(DialogInterface.BUTTON_NEUTRAL, lbl_btnno, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });

        alerta.show();
    }

    private void deleteContact(){
        SQLiteStatement stc;
        stc = bd.compileStatement("DELETE FROM contactos WHERE id = ? ");
        stc.bindString(1, idcontacto);
        stc.execute();
        stc.close();
        bd.close();
        finish();
    }

    public void confDeleteContact(View v){
        confirm();
    }

    private void setData(String idcontacto){
        Map<String, String> map_data = getContactData(idcontacto);
        tv_nombre.setText(map_data.get("nombre")+" "+map_data.get("apellido"));
        tv_celnum.setText(map_data.get("celnum"));

        lat = Double.parseDouble(map_data.get("lat"));
        lon = Double.parseDouble(map_data.get("lon"));

        setGMap();
    }

    private Map<String, String> getContactData(String idcontacto){
        Map<String, String> map_data = new HashMap<String, String>();
        map_data.put("apellido", "");
        map_data.put("nombre", "");
        map_data.put("celnum", "");
        map_data.put("lat", "");
        map_data.put("lon", "");

        Cursor fila = bd.rawQuery("SELECT * FROM contactos WHERE id = "+idcontacto, null);
        if(fila.moveToFirst()){
            map_data.put("apellido", fila.getString(1));
            map_data.put("nombre", fila.getString(2));
            map_data.put("celnum", fila.getString(3));
            map_data.put("lat", fila.getString(4));
            map_data.put("lon", fila.getString(5));
        }

        return map_data;
    }

    private void setGMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa2);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa2 = googleMap;
        // 51.48257659999999, -0.007658900000024005
        mapa2.setMapType(googleMap.MAP_TYPE_NORMAL);
        mapa2.getUiSettings().setZoomControlsEnabled(true);
        LatLng ciudad = new LatLng(lat, lon);
        mapa2.moveCamera(CameraUpdateFactory.newLatLngZoom(ciudad, 18));
        MarkerOptions marcador = new MarkerOptions().title("^").position(ciudad);
        mapa2.addMarker(marcador);
    }

    public void btnBack(View v){
        bd.close();
        finish();
    }
}
