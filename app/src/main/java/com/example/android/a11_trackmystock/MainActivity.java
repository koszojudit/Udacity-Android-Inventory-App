package com.example.android.a11_trackmystock;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.a11_trackmystock.data.ProductContract;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int PRODUCT_LOADER = 1;

    // Recycler view for the products list
    private ListView productListView;

    // Adapter for the products list
    private ProductCursorAdapter cursorAdapter;

    // Empty state view
    private View emptyStateView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //deleteAllItems();

        // Toolbar settings - set title
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the list data
        productListView = (ListView) findViewById(R.id.product_list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        emptyStateView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyStateView);

        // Set up adapter to create a list item for each row of data in the Cursor
        cursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(cursorAdapter);

        // Setup the item click listener to the ListView
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Toast.makeText(MainActivity.this, "ID = " + id, Toast.LENGTH_SHORT).show();

                // Create new intent to go to DetailsActivity
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);

                // Form the content URI for the list item that was clicked on by appending the "id" onto the CONTENT_URI
                // e.g. product id = 2 -- content://com.example.android.a11_trackmystock/products/2
                Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the DetailsActivity to display the data for the current item
                startActivity(intent);
            }
        });

        // Start the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_insert_dummy:
                insertItem();
                return true;
            case R.id.action_delete_all_products:
                // Delete all items
                if (cursorAdapter.getCount() != 0) {
                    showDeleteConfirmationDialog();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies the columns from the database we care about.
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,           // Parent activity context
                ProductContract.ProductEntry.CONTENT_URI,       // The content URI
                projection,                     // The columns to return
                null,                           // The columns for the WHERE clause
                null,                           // The values for the WHERE clause
                null);                          // The sort order)
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the adapter with a new cursor containing the updated products data
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        cursorAdapter.swapCursor(null);
    }

    private void insertItem() {
        // Create a ContentValues object where column names are the keys, and the new dummy item has its values.
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, getString(R.string.dummy_image_uri));
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, "Example Product");
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, 9.05);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, 15);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME, "IKEA");
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL, "hello@supplier.com");
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE, "00 36 20 111 1111");

        Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
        Log.v(LOG_TAG, "Dummy product: " + newUri);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_delete_all));
        builder.setPositiveButton(getString(R.string.dialog_reply_delete_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllItems();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_reply_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllItems() {
        if (ProductContract.ProductEntry.CONTENT_URI != null) {
            int rowsDeleted = getContentResolver().delete(
                    ProductContract.ProductEntry.CONTENT_URI,
                    null,
                    null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.toast_error_delete_all),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_confirm_delete_all),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}
