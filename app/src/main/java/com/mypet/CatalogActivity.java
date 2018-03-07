package com.mypet;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mypet.data.PetContract.PetEntry;

import java.util.Arrays;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final String ANONYMOUS = "anonymous";
    private String userName;

    PetCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_main);

        userName = ANONYMOUS;
        FirebaseApp.initializeApp(this);
        mFirebaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //find list view to populate
        final ListView listView = (ListView) findViewById(R.id.cursor_view);

        //find an empty view to display when there is no data
        final View emptyView = findViewById(R.id.empty_view);

        //set empty view
        listView.setEmptyView(emptyView);

        //set up cursor adapter, we pass a null because there is no data yet until
        // the loader finishes from the background
        adapter = new PetCursorAdapter(this, null);
        listView.setAdapter(adapter);

        //set up a click listener that is called when item on the listView is clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //create new intent to go to @link EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                //form the content URI that represent specific pet that was clicked on,
                // by appending the id passed as input to this method
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                //set up the Uri on the data field of the intent
                intent.setData(currentPetUri);

                //launch the @EditorActivity
                startActivity(intent);
            }
        });

        //initialize loader
        getLoaderManager().initLoader(PET_LOADER, null, this);


        //initialize the listener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is signed in, get the @onSingingInitialize helper method and pass in user
                    onSigningInitialize(user.getDisplayName());
                } else {
                    //user is signed out by calling @onSignOut and @setProvider methods
                    onSignOut();
                    setProvider();
                }
            }
        };
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Login Cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    //helper method for signing
    private void onSigningInitialize(String username) {
        userName = username;
    }

    //helper method for sign out
    private void onSignOut() {
        userName = ANONYMOUS;
    }

    //helper method for provider
    private void setProvider() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_insertAll:
                //insert method helper
                insertDummyData();
                return true;

            case R.id.action_deleteAll:
                deleteAllData();
                return true;
            case R.id.action_sign_out:
                AuthUI.getInstance().signOut(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void insertDummyData() {
        //creating a content values with key - value pair
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.MALE_PET_GENDER);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 14);

        //inserting values to the database
        getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    private void deleteAllData() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI,
                null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.pet_deletion_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.pet_deletion_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED};

        return new CursorLoader(getApplicationContext(), PetEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //updates PetCursorAdapter with updated data
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //it is called when the data needs to be deleted
        adapter.swapCursor(null);
    }
}
