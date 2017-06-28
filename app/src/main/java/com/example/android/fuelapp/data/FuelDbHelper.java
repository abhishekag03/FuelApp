package com.example.android.fuelapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vishaal on 28/6/17.
 */

public class FuelDbHelper extends SQLiteOpenHelper
{

    private static final String DATABASE_NAME="fuelDb.db";

    private static final int DATABASE_VERSION=1;


    public FuelDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        final String CREATE_TABLE;

        CREATE_TABLE= "CREATE TABLE "+ FuelContract.FuelEntry.TABLE_NAME+" ( "
                + FuelContract.FuelEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FuelContract.FuelEntry.COLUMN_TIME_FILLED+" TEXT NOT NULL, "
                + FuelContract.FuelEntry.COLUMN_MONEY+" TEXT NOT NULL, "
                + FuelContract.FuelEntry.COLUMN_LITRES+" INTEGER NOT NULL, "
                + FuelContract.FuelEntry.COLUMN_LATITUDE+" TEXT NOT NULL, "
                + FuelContract.FuelEntry.COLUMN_LONGITUDE+" TEXT NOT NULL "
                +");";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+ FuelContract.FuelEntry.TABLE_NAME);
        onCreate(db);

    }
}
