package com.kekmicrosys.qallme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql_stc1 = "CREATE TABLE usuario(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "apellido TEXT, nombre TEXT, celnum TEXT)";
        String sql_stc2 = "CREATE TABLE contactos(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "apellido TEXT, nombre TEXT, celnum TEXT, latitud TEXT, longitud TEXT)";
        db.execSQL(sql_stc1);
        db.execSQL(sql_stc2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
