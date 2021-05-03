package com.example.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.inventoryapp.Data.ProductContract;

import java.sql.Blob;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // return the list item view
        return LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameView = (TextView)view.findViewById(R.id.name);
        TextView supplierView = (TextView)view.findViewById(R.id.supplier);
        TextView priceView = (TextView)view.findViewById(R.id.pric_e);
        TextView isAvailable = (TextView)view.findViewById(R.id.is_available);
        ImageView productPhoto = (ImageView)view.findViewById(R.id.photo);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int supplierColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);

        // Read the product attributes from the Cursor for the current product
        String currentProduct = cursor.getString(nameColumnIndex);
        int currentPrice = cursor.getInt(priceColumnIndex);
        int currentQuantity = cursor.getInt(quantityColumnIndex);
        String currentSupplier = cursor.getString(supplierColumnIndex);
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE));
        Bitmap btm = BitmapFactory.decodeByteArray(blob,0,blob.length);
        productPhoto.setImageBitmap(btm );
        // Update the TextViews with the attributes for the current product
        nameView.setText(currentProduct);
        supplierView.setText("by " +  currentSupplier);
        priceView.setText("Rs. " + currentPrice);
        if(currentQuantity > 1){
            isAvailable.setText("Hurry, only "+currentQuantity+" left");
        }
        else{
            isAvailable.setText("Oops, Not Available :(");
        }
    }
}
