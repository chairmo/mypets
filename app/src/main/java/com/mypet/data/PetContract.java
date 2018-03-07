package com.mypet.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by chairmo on 2/2/2018.
 */

public final class PetContract {

    //create content authority which is the package for the app
    public static final String CONTENT_AUTHORITY = "com.android.mypet";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_URI_AUTHORITY= Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PETS = "pets";

    //defined empty constructor to avoid accidental modification of the contract
    public PetContract(){}
    /*
    Inner class defining string constants
     */
    public static final class PetEntry implements BaseColumns{
        //content uri with access to pets in the database
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI_AUTHORITY, PATH_PETS);

        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_WEIGHT = "weight";

        public static final int UNKNOWN_PET_GENDER = 0;
        public static final int MALE_PET_GENDER = 1;
        public static final int FEMALE_PET_GENDER = 2;

        //create cursor base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + TABLE_NAME;

        //create cursor base item type for a single entries
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + TABLE_NAME;

        //for building Uri's on insertion
        public static boolean isValidGender(int gender){
            return gender == UNKNOWN_PET_GENDER || gender == MALE_PET_GENDER ||
                    gender == FEMALE_PET_GENDER;
        }

    }
}
