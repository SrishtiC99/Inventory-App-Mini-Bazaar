package com.example.inventoryapp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ProductDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "bazaar.db";

    public ProductDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create tables for all the products
        String SQL_CREATE_ENTRIES = "CREATE TABLE " +
                ProductContract.ProductEntry.TABLE_NAME + " (" +
                ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT, " +
                ProductContract.ProductEntry.COLUMN_USER_ID + " TEXT, " +
                ProductContract.ProductEntry.COLUMN_PASSWORD + " TEXT, " +
                ProductContract.ProductEntry.COLUMN_IMAGE + " BLOB NOT NULL)";

        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);

        // Create table for log in information;
        String SQL_CREATE_ENTRIES_FOR_LOGIN = "CREATE TABLE " +
                ProductContract.ProductEntry.TABLE_NAME_LOGIN + " (" +
                ProductContract.ProductEntry.LOGIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductContract.ProductEntry.COLUMN_USER_ID + " TEXT, " +
                ProductContract.ProductEntry.COLUMN_PASSWORD + " TEXT, " +
                ProductContract.ProductEntry.COLUMN_IS_SUPPLIER + " INTEGER NOT NULL ) ";

        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES_FOR_LOGIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME_LOGIN);
        onCreate(sqLiteDatabase);
    }
}
