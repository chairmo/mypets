package com.mypet;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.mypet.data.PetContract;

/**
 * Created by chairmo on 2/6/2018.
 */

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup,
                false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
       //field to populate the list_item inflated template
        TextView nameView = (TextView) view.findViewById(R.id.name_view);
        TextView breedView = (TextView) view.findViewById(R.id.summary_view);


        //find the column of the pet attribute from the database table
        int nameColumn = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        int breedColummn = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);

        //extract properties from cursor
        String petName = cursor.getString(nameColumn);
        String petBreed = cursor.getString(breedColummn);


        //populate field with extracted property
        nameView.setText(petName);
        breedView.setText(petBreed);
    }
}

