package com.example.android.a11_trackmystock.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.content.RestrictionEntry.TYPE_INTEGER;
import static android.util.TypedValue.TYPE_FLOAT;
import static java.sql.Types.FLOAT;

/**
 * Created by koszojudit on 2017. 07. 20..
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    // Database version
    private static final int DATABASE_VERSION = 1;

    // Name of the database file
    private static final String DATABASE_NAME = "products.db";

    // SQL statement to create the products table

    final String TEXT = " TEXT";
    final String INTEGER = " INTEGER";
    final String REAL = " REAL";
    final String NOT_NULL = " NOT NULL";
    final String COMMA_SEP = ", ";


    String SQL_CREATE_TABLE =  "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " ("
            + ProductContract.ProductEntry._ID + INTEGER + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
            + ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + TEXT + NOT_NULL + COMMA_SEP
            + ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE + TEXT + NOT_NULL + COMMA_SEP
            + ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + REAL + NOT_NULL + COMMA_SEP
            + ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + INTEGER + NOT_NULL + " DEFAULT 0" + COMMA_SEP
            + ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME + TEXT + NOT_NULL + COMMA_SEP
            + ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + TEXT + NOT_NULL + COMMA_SEP
            + ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE + TEXT + NOT_NULL
            + ")";

    // Default constructor for a new instance of DbHelper
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method to create the database for the first time

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_TABLE);
    }

    // Method to call when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1 - nothing to do
    }
}
