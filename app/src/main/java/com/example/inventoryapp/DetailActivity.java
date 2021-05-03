package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.Data.ProductContract;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Uri productUri;
    private TextView detailProductNameView;
    private TextView detailProductPriceView;
    private Button orderButton;
    private ImageView photo;
    private static final int LOADER_ID = 0;
    private int quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        productUri = intent.getData();

        detailProductNameView = (TextView)findViewById(R.id.detail_product_name);
        detailProductPriceView = (TextView)findViewById(R.id.detail_product_price);
        orderButton = (Button)findViewById(R.id.place_order);
        photo = (ImageView)findViewById(R.id.detail_product_image);

        getSupportLoaderManager().initLoader(LOADER_ID, null, DetailActivity.this);
        orderButton.setVisibility(View.INVISIBLE);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // decrease the quantity of this product by one
                if(quantity > 1) {
                    quantity--;
                    placeOrder();
                }
            }
        });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_IMAGE
        };
        return new CursorLoader(this,
                productUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Extract out the value from the Cursor for the given column index
            String detailProductName = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME));
            int price = data.getInt(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE));
            quantity = data.getInt(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
            String supplier = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER));

            detailProductNameView.setText(detailProductName);
            detailProductPriceView.setText("Rs. " + price );
            orderButton.setVisibility(View.VISIBLE);
            byte[] blob = data.getBlob(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE));
            Bitmap btm = BitmapFactory.decodeByteArray(blob,0,blob.length);
            photo.setImageBitmap(btm );
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private void placeOrder(){
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        int rowsAffected = getContentResolver().update(productUri, values, null, null);
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show();
        } else {
            // Send a message to the buyer.
            // get the Name and phone number of user from shared preferences

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String keyForNumber = getString(R.string.phone_number_key);
            String defaultNumber = "9631532600";
            String UserPhoneNumber = prefs.getString(keyForNumber, defaultNumber);

            String keyForName = "user_name";
            String defaultName = "Unknown";
            String Name = prefs.getString(keyForName, defaultName);
            // Send Message
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address",UserPhoneNumber);
            String msgtext = "Hey, " + Name + " thanks for order from this App!\n" + "You can track your order from here.";
            smsIntent.putExtra("sms_body",msgtext);
            smsIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(smsIntent);
            Toast.makeText(this, "Order Placed", Toast.LENGTH_SHORT).show();
        }
    }
}