package com.jhj.dev.wifi.server.mydatabase;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author 韩吉
 */
public class MyDatabase extends SQLiteOpenHelper {

    public MyDatabase(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    public MyDatabase(Context context, String name, int version) {
        this(context, name, null, version);
        // TODO Auto-generated constructor stub
    }

    public MyDatabase(Context context, String name) {
        this(context, name, 1);
        // TODO Auto-generated constructor stub
    }

    public MyDatabase(Context context, String name, CursorFactory factory, int version,
                      DatabaseErrorHandler errorHandler)
    {
        super(context, name, factory, version, errorHandler);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

        db.execSQL("create table PointsSql (ssid text not null,mac text not null," +
                   "Lat text not null,Lng text not null,Level text not null,address text not null));");

        db.execSQL(
                "create table HistoryPointsSql(address text not null,Lat text not null,Lng text not null);");

        db.execSQL("create table MacInfoSql(mac text not null,manufacturer text not null);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

}
