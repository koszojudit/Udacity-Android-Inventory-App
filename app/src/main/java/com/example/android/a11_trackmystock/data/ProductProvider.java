package com.example.android.a11_trackmystock.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.a11_trackmystock.R;

/**
 * Created by koszojudit on 2017. 07. 20..
 */

public class ProductProvider extends ContentProvider {

    // Tag for the log messages
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    //URI matcher code for the products table
    private static final int PRODUCTS = 100;

    // URI matcher code for a single item in the products table
    private static final int PRODUCT_ID = 101;

    // Database Helper object that will provide access to the database
    private ProductDbHelper mDbHelper;

    // Column Tags to be validated
    private static final String TAG_NAME = "name";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_PRICE = "price";
    private static final String TAG_QUANTITY = "quantity";
    private static final String TAG_SUPPLIER_NAME = "supplier";
    private static final String TAG_SUPPLIER_EMAIL = "email";
    private static final String TAG_SUPPLIER_PHONE = "phone";


    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    // Static initializer. This is run the first time anything is called from this class.
    static {
        // Content URI of the form "content://package com.example.android.a11_trackmystock/products" will map to the
        // integer code PRODUCTS. This URI is used to provide access to multiple table rows.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);

        // Content URI of the form "content://package com.example.android.a11_trackmystock/products/#" will map to the
        // integer code #PRODUCT_ID, and is used to provide access to ONE single row of the table.
        // The "#" wildcard can be substituted for an integer. Ex - content://package com.example.android.a11_trackmystock/products/5
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    // Initialize the provider - create and initialize a ProductDbHelper object to gain access to the database
    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    // READ method - read from the database
    // by performing query for given URI and loading the cursor with results fetched from table
    // Returned result can have multiple rows or a single row, depending on given URI.

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get instance of readable database
        SQLiteDatabase sqLiteDBReadable = mDbHelper.getReadableDatabase();

        // Cursor to hold the query result
        Cursor cursor;

        // Check if the uri matches to a specific URI CODE
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                cursor = sqLiteDBReadable.query(ProductContract.ProductEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = sqLiteDBReadable.query(ProductContract.ProductEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri));
        }

        // Set notification URI on Cursor so it knows when to update in the event the data in cursor changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    // INSERT method - insert row(s) to the database

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        long id;
        String columnsToValidate = TAG_NAME + "|" + TAG_IMAGE + "|" + TAG_PRICE + "|"
                + TAG_QUANTITY + "|" + TAG_SUPPLIER_NAME + "|" + TAG_SUPPLIER_EMAIL + "|" + TAG_SUPPLIER_PHONE;

        boolean isValidInput = validateInput(contentValues, columnsToValidate);

        if (isValidInput) {
            // Get instance of writable database
            SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();

            // Insert new record with given values
            id = sqLiteDBWritable.insert(ProductContract.ProductEntry.TABLE_NAME,
                    null, contentValues);
        } else {
            id = -1;
        }

        // Check if ID is -1, which means record insert has failed
        if (id == -1) {
            Log.e(LOG_TAG, (getContext().getString(R.string.log_error_insert, uri)));
            return null;
        }

        // Notify all listeners that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID of the newly inserted row appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    // UPDATE method
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);

            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri));
        }

    }

    // Validate user input for updates, and apply the updates
    public int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        String columnsToValidate = null;
        StringBuilder stringBuilder = new StringBuilder();
        final String SEPARATOR = "|";
        int rowsUpdated = 0;

        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        } else {
            if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
                stringBuilder.append(TAG_NAME);
            } else if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE)) {
                stringBuilder.append(SEPARATOR).append(TAG_IMAGE);
            } else if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
                stringBuilder.append(SEPARATOR).append(TAG_PRICE);
            } else if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
                stringBuilder.append(SEPARATOR).append(TAG_QUANTITY);
            } else if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME)) {
                stringBuilder.append(SEPARATOR).append(TAG_SUPPLIER_NAME);
            } else if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL)) {
                stringBuilder.append(SEPARATOR).append(TAG_SUPPLIER_EMAIL);
            } else if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE)) {
                stringBuilder.append(SEPARATOR).append(TAG_SUPPLIER_PHONE);
            }

            columnsToValidate = stringBuilder.toString();
            boolean isValidInput = validateInput(contentValues, columnsToValidate);

            if (isValidInput) {
                // Get instance of writable database
                SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();

                // Perform the update on the database and get the number of rows affected
                rowsUpdated = sqLiteDBWritable.update(ProductContract.ProductEntry.TABLE_NAME,
                        contentValues, selection, selectionArgs);

                // If 1 or more rows were updated, then notify all listeners that the data at the
                // given URI has changed
                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
            }
            return rowsUpdated;
        }
    }

    // Validate input for INSERT and UPDATE methods
    public boolean validateInput(ContentValues values, String columns) {

        String [] columnArgs = columns.split("|");
        String productName = null;
        String productImage;
        Double productPrice = null;
        Integer quantity = null;
        String supplier = null;
        String supplierEmail = null;
        String supplierPhone = null;


        for (int i = 0; i < columnArgs.length; i++ ) {

            if (columnArgs[i].equals(TAG_NAME)) {
                // Check if Product Name is not null
                productName = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
                if (productName == null || productName.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_name));
                }
            }
            if (columnArgs[i].equals(TAG_IMAGE)) {
                // Check if Product Image is not null
                productImage = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
                if (productImage == null || productImage.equals("")) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_image));
                }
            }
            else if (columnArgs[i].equals(TAG_PRICE)) {
                // Check if Price is provided
                productPrice = values.getAsDouble(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
                if (productPrice == null || productPrice < 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_price));
                }
            }
            else if (columnArgs[i].equals(TAG_QUANTITY)) {
                quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
                if (quantity == null || quantity < 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_quantity));
                }
            }
            else if (columnArgs[i].equals(TAG_SUPPLIER_NAME)) {
                supplier = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
                if (supplier == null || supplier.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_supplier_name));
                }
            }
            else if (columnArgs[i].equals(TAG_SUPPLIER_EMAIL)) {
                supplierEmail = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL);
                if (supplierEmail == null || supplierEmail.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_supplier_email));
                }
            }
            else if (columnArgs[i].equals(TAG_SUPPLIER_PHONE)) {
                supplierPhone = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE);
                if (supplierPhone == null || supplierPhone.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.exception_invalid_supplier_phone));
                }
            }
        }

        return true;
    }

    // DELETE from the table

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get instance of writable database
        SQLiteDatabase sqLiteDBWritable = mDbHelper.getWritableDatabase();

        // Number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        switch(match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = sqLiteDBWritable.delete(ProductContract.ProductEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqLiteDBWritable.delete(ProductContract.ProductEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri));
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    // Return the MIME type of data for the content URI
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                // throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri));
        }
    }
}
