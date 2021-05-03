package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.Data.ProductContract;
import com.example.inventoryapp.Data.ProductDbHelper;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "LoginActivity";
    public boolean isSupplier = false;
    public String userId;
    public String password;
    private ProgressBar loadingIndicator;
    private ProductDbHelper dbHelper;
    private static final int LOADER_ID = 0;
    private boolean isRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText usernameText = (EditText) findViewById(R.id.username);
        EditText passwordText = (EditText) findViewById(R.id.password);
        CheckBox checkBox = (CheckBox) findViewById(R.id.supplier_login);
        loadingIndicator = (ProgressBar) findViewById(R.id.loading);
        Button signInButton = (Button) findViewById(R.id.login);
        Button signUpButton = (Button) findViewById(R.id.make_account);
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        dbHelper = new ProductDbHelper(LoginActivity.this);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadingIndicator.setVisibility(View.VISIBLE);
                userId = usernameText.getText().toString();
                password = passwordText.getText().toString();
                isSupplier = checkBox.isChecked();
                if (Register()) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERID", userId);
                    intent.putExtra("PASSWORD", password);
                    intent.putExtra("IS_SUPPLIER", isSupplier);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                }

            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingIndicator.setVisibility(View.VISIBLE);
                userId = usernameText.getText().toString();
                password = passwordText.getText().toString();
                isSupplier = checkBox.isChecked();
                getSupportLoaderManager().initLoader(LOADER_ID, null, LoginActivity.this);
            }
        });

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Define a projection that specifies the columns that we care about
        String[] projection = {
                ProductContract.ProductEntry.COLUMN_USER_ID,
                ProductContract.ProductEntry.COLUMN_PASSWORD
        };
        String selection = ProductContract.ProductEntry.COLUMN_USER_ID + "=?" ;
        String[] selectionArgs = new String[]{userId};
        // The loader will execute the content provider's query method on a background thread
        return new CursorLoader(
                this,                               // Parent activity context
                ProductContract.ProductEntry.CONTENT_URI_LOGIN,   // Provider Content URI to query
                projection,                                 // columns to include in the resultant cursor
                selection,
                selectionArgs,                            // No selection arguments
                null);                              // Sort Order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        loadingIndicator.setVisibility(View.INVISIBLE);
        if (data.moveToFirst()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USERID", userId);
            intent.putExtra("PASSWORD", password);
            intent.putExtra("IS_SUPPLIER", isSupplier);
            startActivity(intent);
        }else {
            Toast.makeText(LoginActivity.this, R.string.invalid_username, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private boolean Register() {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Input is null");
            return false;
        }
        int supplier = 0;
        if (isSupplier)
            supplier = 1;
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_USER_ID, userId);
        values.put(ProductContract.ProductEntry.COLUMN_PASSWORD, password);
        values.put(ProductContract.ProductEntry.COLUMN_IS_SUPPLIER, supplier);
        Uri newRowUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI_LOGIN, values);
        if (newRowUri == null) {
            Toast.makeText(this, "Something is Wrong, Try Again!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            loadingIndicator.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}