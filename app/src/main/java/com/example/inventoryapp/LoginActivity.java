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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.Data.ProductContract;
import com.example.inventoryapp.Data.ProductDbHelper;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "LoginActivity";
    private boolean isSupplier = false;
    private String emailId;
    private String password;
    private String userFullName;
    private ProgressBar loadingIndicator;
    private ProductDbHelper dbHelper;
    private static final int LOADER_ID = 0;
    private boolean isRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LinearLayout SignInLayout = (LinearLayout) findViewById(R.id.account_exist);
        LinearLayout RegisterLayout = (LinearLayout) findViewById(R.id.new_account);

        Button signInButton = (Button) findViewById(R.id.signIn_button);
        Button signUpButton = (Button) findViewById(R.id.signUp_button);
        EditText emailIdText = (EditText) findViewById(R.id.email);
        EditText emailIdTextNew = (EditText) findViewById(R.id.email_new);
        EditText passwordText = (EditText) findViewById(R.id.password);
        EditText passwordTextNew = (EditText) findViewById(R.id.password_new);
        EditText FullNameText = (EditText) findViewById(R.id.full_name);
        Switch isSupplierSwitch = (Switch) findViewById(R.id.is_Supplier);
        Switch isSupplierSwitchNew = (Switch) findViewById(R.id.is_Supplier_new);


        TextView SignInText = (TextView) findViewById(R.id.signIn_text);
        TextView SignUpText = (TextView) findViewById(R.id.signUp_text);

        SignInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterLayout.setVisibility(View.INVISIBLE);
                SignInLayout.setVisibility(View.VISIBLE);
            }
        });
        SignUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInLayout.setVisibility(View.INVISIBLE);
                RegisterLayout.setVisibility(View.VISIBLE);
            }
        });
        loadingIndicator = (ProgressBar) findViewById(R.id.loading);
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        dbHelper = new ProductDbHelper(LoginActivity.this);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadingIndicator.setVisibility(View.VISIBLE);
                emailId = emailIdTextNew.getText().toString();
                password = passwordTextNew.getText().toString();
                isSupplier = isSupplierSwitchNew.isChecked();
                if (Register()) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERID", emailId);
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
                emailId = emailIdText.getText().toString();
                password = passwordText.getText().toString();
                isSupplier = isSupplierSwitch.isChecked();
                getSupportLoaderManager().initLoader(LOADER_ID, null, LoginActivity.this);
            }
        });

        if(isSupplier){
            Log.e(TAG, "TRUE");
        }else{
            Log.e(TAG, "FALSE");
        }
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
        String[] selectionArgs = new String[]{emailId};
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
            intent.putExtra("USERID", emailId);
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
        if (TextUtils.isEmpty(emailId) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Input is null");
            return false;
        }
        int supplier = 0;
        if (isSupplier)
            supplier = 1;
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_USER_ID, emailId);
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