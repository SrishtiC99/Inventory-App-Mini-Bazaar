package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.Data.ProductContract;
import com.example.inventoryapp.Data.ProductDbHelper;

import java.io.ByteArrayOutputStream;
import java.net.URI;

public class SupplierActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private String userId;
    private String password;
    private int intQuantity;
    private EditText productNameView;
    private EditText priceView;
    private TextView quantityView;
    private EditText supplierNameView;
    private ImageView productImageView;
    private byte[] imageBlob = null;
    private Uri currentProductUri;
    private boolean mProductHasChanged = false;
    private static final int EXISTING_PRODUCT_LOADER = 0;
    static final int REQUEST_CAMERA = 1;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mPetHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        currentProductUri = intent.getData();
        userId = intent.getExtras().getString("USERID");
        password = intent.getExtras().getString("PASSWORD");
        boolean isSupplier = intent.getExtras().getBoolean("IS_SUPPLIER", false);
        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (currentProductUri == null) {
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle("Edit Product");

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, SupplierActivity.this);
        }
        
        productNameView = (EditText)findViewById(R.id.product_name);
        priceView = (EditText)findViewById(R.id.price);
        quantityView = (TextView)findViewById(R.id.quantity);
        supplierNameView = (EditText)findViewById(R.id.supplier_name);
        intQuantity = Integer.parseInt(quantityView.getText().toString());
        Button increaseQ = (Button)findViewById(R.id.inc_quantity);
        increaseQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intQuantity ++;
                quantityView.setText(String.valueOf(intQuantity));
            }
        });
        Button decreaseQ = (Button)findViewById(R.id.dec_quantity);
        decreaseQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(intQuantity==0) return;
                intQuantity --;
                quantityView.setText(String.valueOf(intQuantity));
            }
        });
        productImageView = (ImageView) findViewById(R.id.product_image);
        productImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(intent, REQUEST_CAMERA);
            }
        });
        productNameView.setOnTouchListener(mTouchListener);
        priceView.setOnTouchListener(mTouchListener);
        quantityView.setOnTouchListener(mTouchListener);
        supplierNameView.setOnTouchListener(mTouchListener);
        productImageView.setOnTouchListener(mTouchListener);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                productImageView.setImageBitmap(image);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imageBlob = stream.toByteArray();
            }
        }
    }
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveProduct(){
        String productName = productNameView.getText().toString().trim();
        String priceV = priceView.getText().toString();
        String supplierName = supplierNameView.getText().toString().trim();
        productImageView.invalidate();
        BitmapDrawable drawable = (BitmapDrawable) productImageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bArray = bos.toByteArray();

        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (currentProductUri == null && TextUtils.isEmpty(productName) && TextUtils.isEmpty(supplierName) && TextUtils.isEmpty(priceV)) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
        int price = Integer.parseInt(priceV);
        /*
        We won't need this
        ProductDbHelper dbHelper = new ProductDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

         */

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME , productName);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, intQuantity);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierName);
        values.put(ProductContract.ProductEntry.COLUMN_USER_ID, userId);
        values.put(ProductContract.ProductEntry.COLUMN_PASSWORD, password);
        values.put(ProductContract.ProductEntry.COLUMN_IMAGE, bArray);
        /*
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
         */
        if(currentProductUri == null){
            Uri newRowUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if(newRowUri == null){
                Toast.makeText(this, "Error with saving product",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"Product saved " , Toast.LENGTH_SHORT).show();
            }
        }else{
            // Otherwise this is an EXISTING product, so update the product with content URI: currentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because currentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Update Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "Updated Successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        // Implement this method
        // Only perform the delete if this is an existing pet.
        if (currentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.supplier_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save:
                saveProduct();
                // Exit activity
                finish();
                return true;
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(SupplierActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(SupplierActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                ProductContract.ProductEntry.COLUMN_IMAGE
        };
        // The loader will execute the content provider's query method on a background thread
        return new CursorLoader(
                this,                               // Parent activity context
                currentProductUri,   // Provider Content URI to query
                projection,                                 // columns to include in the resultant cursor
                null,                               // No selection clause
                null,                            // No selection arguments
                null);                              // Sort Order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);

            // Extract out the value from the Cursor for the given column index
            String currentProduct = data.getString(nameColumnIndex);
            int currentPrice = data.getInt(priceColumnIndex);
            int currentQuantity = data.getInt(quantityColumnIndex);
            String currentSupplier = data.getString(supplierColumnIndex);

            // Update the views on the screen with the values from the database
            productNameView.setText(currentProduct);
            priceView.setText(String.valueOf(currentPrice));
            quantityView.setText(String.valueOf(currentQuantity));
            supplierNameView.setText(currentSupplier);
            byte[] blob = data.getBlob(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE));
            Bitmap btm = BitmapFactory.decodeByteArray(blob,0,blob.length);
            productImageView.setImageBitmap(btm );
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        productNameView.setText(" ");
        priceView.setText(" ");
        quantityView.setText("0");
        supplierNameView.setText(" ");
    }
}