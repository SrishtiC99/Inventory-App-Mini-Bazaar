package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.Data.ProductContract;
import com.example.inventoryapp.Data.ProductDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private String userId;
    private String password;
    private boolean isSupplier = false;
    private static final int PRODUCT_LOADER =0;
    private ProductCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent.hasExtra("USERID"))
            userId = intent.getExtras().getString("USERID");
        if(intent.hasExtra("PASSWORD"))
            password = intent.getExtras().getString("PASSWORD");
        if(intent.hasExtra("IS_SUPPLIER"))
            isSupplier = intent.getExtras().getBoolean("IS_SUPPLIER",false);
        FloatingActionButton fab = findViewById(R.id.fab);
        if(isSupplier){
            fab.setVisibility(View.VISIBLE);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add a new product
                Intent intent = new Intent(MainActivity.this, SupplierActivity.class);
                intent.putExtra("USERID", userId);
                intent.putExtra("PASSWORD", password);
                intent.putExtra("IS_SUPPLIER", isSupplier);
                startActivity(intent);
            }
        });
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        ProductDbHelper dbHelper = new ProductDbHelper(this);

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_title_text);
        productListView.setEmptyView(emptyView);

        // Set up an adapter to create a list item for each row of product data in the cursor
        // There is no product data yet (until the loader finishes) so pass in null for the cursor
        cursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(cursorAdapter);
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to DetailActivity
                Intent intent;
                Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);
                if(isSupplier){
                    intent = new Intent(MainActivity.this, SupplierActivity.class);

                    // Form the content URI that represents the specific product that was clicked on,
                    // by appending the "id" (passed as input to this method) onto the
                    // {@link ProductEntry#CONTENT_URI}.
                    // For example, the URI would be "content://com.example.inventoryapp/products/2"
                    // if the product with ID 2 was clicked on.

                    // Set the URI on the data field of the intent
                    intent.setData(currentProductUri);
                    intent.putExtra("USERID", userId);
                    intent.putExtra("PASSWORD", password);
                    intent.putExtra("IS_SUPPLIER", isSupplier);
                    // Launch the {@link SupplierActivity} to display the data for the current pet.
                }
                else{
                    intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.setData(currentProductUri);
                }
                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(PRODUCT_LOADER,null,this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Define a projection that specifies the columns that we care about
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_USER_ID,
                ProductContract.ProductEntry.COLUMN_PASSWORD,
                ProductContract.ProductEntry.COLUMN_IMAGE
        };
        String selection = null;
        String[] selectionArgs = null;
        if(isSupplier){
            selection = ProductContract.ProductEntry.COLUMN_USER_ID + "=?" ;
            selectionArgs = new String[]{userId};
        }
        // The loader will execute the content provider's query method on a background thread
        return new CursorLoader(
                this,                               // Parent activity context
                ProductContract.ProductEntry.CONTENT_URI,   // Provider Content URI to query
                projection,                                 // columns to include in the resultant cursor
                selection,                               // No selection clause
                selectionArgs,                            // No selection arguments
                null);                              // Sort Order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // update with this new cursor with containing updated pet data
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        cursorAdapter.swapCursor(null);
    }
    public void insertDummyProduct(){
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME , "Yummy Chocolate");
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, 100);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, 2);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, "Srishti");
        values.put(ProductContract.ProductEntry.COLUMN_USER_ID, userId);
        values.put(ProductContract.ProductEntry.COLUMN_PASSWORD, password);
        // To upload the dummy image, get image resource from drawable folder
        Drawable drawable = getResources().getDrawable(R.drawable.chocolate);
        // Convert Drawable to bitmap
        Bitmap bitmap = drawableToBitmap(drawable);
        // After getting bitmap object, need to convert it to byte array using.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        values.put(ProductContract.ProductEntry.COLUMN_IMAGE, byteArray);
        Uri newRowUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final int width = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().height() : drawable.getIntrinsicHeight();

        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width,
                height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);

        Log.v("Bitmap width - Height :", width + " : " + height);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    public void deleteAllProducts(){
        int rowsDeleted = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from products database");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.dummy_data :
                insertDummyProduct();
                return true;
            case R.id.delete_all :
                deleteAllProducts();
                return true;
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra("IS_SUPPLIER", isSupplier);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}