package com.mypet.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mypet.data.PetContract.PetEntry;

/**
 * Created by chairmo on 2/2/2018.
 */

public class PetDbHelper extends SQLiteOpenHelper {
    //constants for database name and version
    public static final int DATABASE_VERSION =1;
    public static final String DATABASE_NAME = "shelter.db";

    //constructor
    public PetDbHelper(Context context){
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + PetEntry.TABLE_NAME + "(" +
            PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PetEntry.COLUMN_PET_NAME +
            " TEXT NOT NULL, " + PetEntry.COLUMN_PET_BREED + " TEXT, " + PetEntry.COLUMN_PET_GENDER
            + " INTEGER NOT NULL, " + PetEntry.COLUMN_PET_WEIGHT + " TEXT NOT NULL DEFAULT 0);";

    @Override
    public void onCreate(SQLiteDatabase db) {
       db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
