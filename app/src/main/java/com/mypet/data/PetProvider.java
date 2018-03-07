package com.mypet.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mypet.data.PetContract.PetEntry;

/**
 * Created by chairmo on 2/4/2018.
 */

public class PetProvider extends ContentProvider {
    //tag for log messages
    private static final String LOG_TAG = PetProvider.class.getSimpleName();

    //Create database object
    private PetDbHelper dbHelper;

    //set up uriMatcher
    private static final int PETS = 100;
    private static final int PETS_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetEntry.TABLE_NAME, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetEntry.TABLE_NAME + "/#", PETS_ID);
    }

    @Override
    public boolean onCreate() {
        //initialize the database object
        dbHelper = new PetDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArg, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        int matcher = sUriMatcher.match(uri);
        switch (matcher) {
            case PETS:
                //performs query on the pets table
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArg,
                        null, null, sortOrder);
                break;

            case PETS_ID:
                //perform query based on id selection
                // projection = new String[]{PetEntry._ID, PetEntry.COLUMN_PET_NAME};
                selection = PetEntry._ID + " = ?";
                selectionArg = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArg,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_DIR_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown uri " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] strings) {
        //create database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //match uri
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        //case for deleting the pets table or a row in the pets table
        switch (match) {
            case PETS:
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, strings);
                break;
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, strings);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported");
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] strings) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, strings);

            case PETS_ID:
                selection = PetEntry._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, strings);

            default:
                throw new IllegalArgumentException("Pet cannot be updated");
        }
    }

    //helper method for updating pets database
    private int updatePet(Uri uri, ContentValues values, String selection, String[] strings) {
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            {
                if (gender != null && !PetEntry.isValidGender(gender)) {
                    throw new IllegalArgumentException("Pet requires valid gender");
                }
            }
        }
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {

            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            {
                if (weight != null && weight < 0) {
                    throw new IllegalArgumentException("Pet requires a valid weight");
                }
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        //create a database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated = db.update(PetEntry.TABLE_NAME, values, selection, strings);

        //check if row updated is not 0, get content resolver
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    //helper method for insertion into pets database
    private Uri insertPet(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender != null && !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires a valid gender");
        }
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires a valid weight");
        }
        long rowId = db.insert(PetEntry.TABLE_NAME, null, values);
        if (rowId == -1) {
            Log.e(LOG_TAG, "insertPet: failed to insert row " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, rowId);
    }
}

