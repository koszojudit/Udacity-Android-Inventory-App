package com.example.android.a11_trackmystock;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.a11_trackmystock.data.ProductContract;
import com.squareup.picasso.Picasso;

/**
 * Created by koszojudit on 2017. 07. 20..
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 1;
    private Uri currentProductUri;

    /**
     * All UI Components
     */
    private TextView productName;
    private ImageView productImage;
    private TextView productPrice;
    private TextView productQuantity;
    private TextView editQuantityBy;
    private TextView supplierName;
    private TextView supplierEmail;
    private TextView supplierPhone;
    private Button buttonOrder;
    private Button buttonMinus;
    private Button buttonPlus;

    // private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Use getIntent() and getData() to get the associated URI
        currentProductUri = getIntent().getData();

        // Toolbar settings
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_details);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize all UI components
        productName = (TextView) findViewById(R.id.product_name);
        productImage = (ImageView) findViewById(R.id.product_image);
        productPrice = (TextView) findViewById(R.id.product_price);
        productQuantity = (TextView) findViewById(R.id.product_quantity);
        editQuantityBy = (EditText) findViewById(R.id.edit_quantity_by);

        supplierName = (TextView) findViewById(R.id.supplier_name);
        supplierEmail = (TextView) findViewById(R.id.supplier_email);
        supplierPhone = (TextView) findViewById(R.id.supplier_phone);

        buttonOrder = (Button) findViewById(R.id.button_order);
        buttonMinus = (Button) findViewById(R.id.button_minus);
        buttonPlus = (Button) findViewById(R.id.button_plus);


        Intent intent = getIntent();
        currentProductUri = intent.getData();
        if (currentProductUri != null) {
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, DetailsActivity.this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that contains all columns from the database
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,           // Parent activity context
                currentProductUri,                    // The content URI
                projection,                     // The columns to return
                null,                           // The columns for the WHERE clause
                null,                           // The values for the WHERE clause
                null);                          // The sort order)
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {

            DatabaseUtils.dumpCursor(cursor);

            final String name = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_PRODUCT_NAME));
            productName.setText(name);

            String productImageString = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_PRODUCT_IMAGE));
            if (TextUtils.isEmpty(productImageString)) {
                productImage.setImageResource(R.drawable.product_image_placeholder);
            } else {
                Uri productImageUri = Uri.parse(productImageString);
                Picasso.with(this).load(productImageUri).into(productImage);
                Picasso.with(this).load(productImageUri).into(productImage);
            }

            productPrice.setText(Double.toString(cursor.getDouble(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_PRODUCT_PRICE))));

            final int quantity = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_PRODUCT_QUANTITY));
            productQuantity.setText(String.valueOf(quantity));


            supplierName.setText(cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_SUPPLIER_NAME)));

            final String email = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_SUPPLIER_EMAIL));
            supplierEmail.setText(email);

            supplierPhone.setText(cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_SUPPLIER_PHONE)));

            // Set OnClickListeners to the quantity buttons
            buttonMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantityModifier = 1;
                    int userInput = editQuantityBy.getText().toString().trim().length();
                    if (!(userInput == 0)) {
                        quantityModifier = Integer.valueOf(editQuantityBy.getText().toString());
                    }
                    modifyQuantity(currentProductUri, (quantity - quantityModifier));
                }
            });

            buttonPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantityModifier = 1;
                    int userInput = editQuantityBy.getText().toString().trim().length();
                    if (!(userInput == 0)) {
                        quantityModifier = Integer.valueOf(editQuantityBy.getText().toString());
                    }
                    modifyQuantity(currentProductUri, (quantity + quantityModifier));
                }
            });

            buttonOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    orderFromSupplier(email, "New order for " + name);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear all views
        productName.setText("");
        productImage.setImageResource(R.drawable.product_image_placeholder);
        productPrice.setText("");
        productQuantity.setText("");
        supplierName.setText("");
        supplierEmail.setText("");
        supplierPhone.setText("");
    }

    private int modifyQuantity(Uri itemUri, int quantityCount) {
        if (quantityCount < 0) {
            return 0;
        }
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityCount);
        int numRowsUpdated = getContentResolver().update(itemUri, values, null, null);
        return numRowsUpdated;
    }

    public void orderFromSupplier(String mailAddress, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, mailAddress);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // Toolbar menu for the activity

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_product);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete_product:
                confirmDeleteProduct();
                return true;
            case R.id.action_edit:
                editProduct();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void confirmDeleteProduct() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_delete));
        builder.setPositiveButton(getString(R.string.dialog_reply_delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_reply_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Delete product failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Delete product successful",
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    public void editProduct() {
        Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
        intent.setData(currentProductUri);
        startActivity(intent);
    }




}
