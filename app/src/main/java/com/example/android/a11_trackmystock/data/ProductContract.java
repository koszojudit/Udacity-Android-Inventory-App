package com.example.android.a11_trackmystock.data;

import android.content.ContentResolver;
import android.graphics.Path;
import android.net.Uri;
import android.provider.BaseColumns;

import java.net.URI;
import java.net.URL;
import java.util.function.Supplier;

/**
 * Created by koszojudit on 2017. 07. 20. - Schema for product database
 */

public class ProductContract {

    // Empty Constructor - prevents anyone from accidentally instantiating the contract class
    private ProductContract() {}

    // ContentProvider Name (String for content authority)
    public static final String CONTENT_AUTHORITY = "com.example.android.a11_trackmystock";

    // Use CONTENT_AUTHORITY to create the BASE of all URI's which apps will use to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible path (appended to base content URI for possible URI's)
    public static final String PATH_PRODUCTS = "products";


    /**
     * Inner class that defines constant values for the Products database table.
     * Each entry in the table represents a single product.
     */
    public static class ProductEntry implements BaseColumns {

        // Content URI to access the data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // MIME type of the for a list of products
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // MIME type of the CONTENT_URI for a single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;


        // Name of database table
        public static final String TABLE_NAME = "products";

        // Unique ID for the product (only to be used in the database - type: INTEGER
        public final static String _ID = BaseColumns._ID;

        // Product name - type: TEXT/
        public final static String COLUMN_PRODUCT_NAME ="name";

        // Product image URL - Type: TEXT
        public final static String COLUMN_PRODUCT_IMAGE ="image";

        // Product price - Type: REAL
        public final static String COLUMN_PRODUCT_PRICE ="price";

        // Product quantity (in stock) - Type: INTEGER
        public final static String COLUMN_PRODUCT_QUANTITY ="quantity";

        // Supplier name - Type: TEXT
        public final static String COLUMN_SUPPLIER_NAME ="supplier_name";

        // Supplier email - Type: TEXT
        public final static String COLUMN_SUPPLIER_EMAIL ="supplier_email";

        // Supplier phone - Type: TEXT
        public final static String COLUMN_SUPPLIER_PHONE ="supplier_phone";

    }
}
