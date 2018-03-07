package com.mypet;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mypet.data.PetContract.PetEntry;

/**
 * Allows a user to create or edit an existing pet
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 0;
    private Uri currentPetUri;

    //EditText variable for user input details
    private EditText name;
    private EditText breed;
    private EditText weight;

    //EditText variable for gender details
    private Spinner mSpinnerGender;

    //int variable for holding the gender, 0 - unknown, 1 - male, 2 - female
    private int gender = PetEntry.UNKNOWN_PET_GENDER;

    private boolean petHasChanged = false;

  /*  private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            petHasChanged = true;
            return false;
        }
    };
  */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //receiving the intent passed from the item selected
        Intent intent = getIntent();
        if (intent != null) {
            currentPetUri = intent.getData();
            //if the intent does not contain the pet content Uri's then we know we are creating a new pet
            if (currentPetUri == null) {
                setTitle(getString(R.string.add_pet));
                invalidateOptionsMenu();
            } else {
                setTitle(getString(R.string.edit_pet));
                getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
            }
        }

        name = (EditText) findViewById(R.id.pet_name);
        breed = (EditText) findViewById(R.id.pet_breed);
        weight = (EditText) findViewById(R.id.pet_weight);
        mSpinnerGender = (Spinner) findViewById(R.id.spinner_gender);
/*
        name.setOnTouchListener(mTouchListener);
        breed.setOnTouchListener(mTouchListener);
        weight.setOnTouchListener(mTouchListener);
        mSpinnerGender.setOnTouchListener(mTouchListener);
        */

        setSpinnerGender(); //calling the spinner helper method

    }

    //A helper method for a drop down spinner that allows a user to select an option
    private void setSpinnerGender() {
        ArrayAdapter genderSpinner = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        //specify a dropdown layout style, list view with 1 item per line
        genderSpinner.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //Apply the adapter to the spinner
        mSpinnerGender.setAdapter(genderSpinner);

        //set the integer selected for the constant value
        mSpinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String selection = (String) adapterView.getItemAtPosition(pos);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("Male")) {
                        gender = PetEntry.MALE_PET_GENDER;
                    } else if (selection.equals("Female")) {
                        gender = PetEntry.FEMALE_PET_GENDER;
                    } else
                        gender = PetEntry.UNKNOWN_PET_GENDER;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                gender = PetEntry.UNKNOWN_PET_GENDER;
            }
        });
    }

    private void savePet() {
        //get text from the user input and save it in a string of variables,
        // trim for unwanted white spaces
        String mName = name.getText().toString().trim();
        String mBreed = breed.getText().toString().trim();
        String mWeight = weight.getText().toString().trim();
        int nWeight = 0;
        if (!TextUtils.isEmpty(mWeight)) {
            nWeight = Integer.parseInt(mWeight);
        }

       /* if (currentPetUri == null) {

            while (TextUtils.isEmpty(mName) || TextUtils.isEmpty(mWeight)) {
                Toast.makeText(this, getString(R.string.empty_input), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        */

            if (currentPetUri == null &&
                    TextUtils.isEmpty(mName) && TextUtils.isEmpty(mBreed) &&
                    TextUtils.isEmpty(mWeight) && gender == PetEntry.UNKNOWN_PET_GENDER) {
                Toast.makeText(this, getString(R.string.empty_input), Toast.LENGTH_SHORT).show();
            }


        //creating a content values with key - value pair
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, mName);
        values.put(PetEntry.COLUMN_PET_BREED, mBreed);
        values.put(PetEntry.COLUMN_PET_GENDER, gender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, nWeight);

        //inserting values to the database
        if (currentPetUri == null) {
            Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.pet_saved), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentPetUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.error_saving),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.pet_saved),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case R.id.action_save:
                //calling the save method
               savePet();
               // validateInput();
                //exit the activity
                finish();
                return true;

            case android.R.id.home:
                if (!petHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButton =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButton);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};

        return new CursorLoader(this,
                currentPetUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            String nameIndex = cursor.getString(nameColumnIndex);
            String breedIndex = cursor.getString(breedColumnIndex);
            int genderIndex = cursor.getInt(genderColumnIndex);
            int weightIndex = cursor.getInt(weightColumnIndex);

            name.setText(nameIndex);
            breed.setText(breedIndex);
            String weightText = "" + weightIndex;
            weight.setText(weightText);

            switch (genderIndex) {
                case PetEntry.MALE_PET_GENDER:
                    mSpinnerGender.setSelection(1);
                    break;
                case PetEntry.FEMALE_PET_GENDER:
                    mSpinnerGender.setSelection(2);
                    break;
                default:
                    mSpinnerGender.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        name.setText("");
        breed.setText("");
        weight.setText("");
        mSpinnerGender.setSelection(0);

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener
                                                  discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_data);
        builder.setPositiveButton(R.string.discard_dialog, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!petHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(dialogClick);
    }

    //show confirmation before deleting pet from the database
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.do_you_want_to_delete);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                delePet();
            }
        });
        builder.setNegativeButton(R.string.cancel_deletion, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //delete a pet from database
    private void delePet() {
        if (currentPetUri != null) {
            int rowsDeleted = getContentResolver().delete(currentPetUri, null,
                    null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.pet_deletion_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.pet_deletion_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
