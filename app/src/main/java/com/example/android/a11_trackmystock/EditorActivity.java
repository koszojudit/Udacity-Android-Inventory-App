package com.example.android.a11_trackmystock;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.a11_trackmystock.data.ProductContract;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by koszojudit on 2017. 07. 20..
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int PRODUCT_LOADER = 0;
    private static final int IMAGE_REQUEST_CODE = 0;
    private Uri currentProductUri;

    // Relevant views to modify in the activity
    private EditText editProductName;
    private ImageView editProductImage;
    private ImageView addImage;
    private EditText editProductPrice;
    private TextView editProductQuantity;
    private EditText editSupplierName;
    private EditText editSupplierEmail;
    private EditText editSupplierPhone;

    private Uri imageUri;

    // User input we can extract from fields
    private String inputProductName;
    private Double inputProductPrice;
    private int inputProductQuantity;
    private String inputSupplierName;
    private String inputSupplierEmail;
    private String inputSupplierPhone;

    // Boolean flag that keeps track of whether the product has been edited (true) or not (false)
    private boolean mProductHasChanged = false;
    // Listen for any user touches on a view, implying that they are modifying the view (boolean turns to true)
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
        setContentView(R.layout.activity_editor);

        // Use getIntent() and getData() to get the associated URI
        currentProductUri = getIntent().getData();

        // Toolbar settings
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_editor);
        setSupportActionBar(myToolbar);
        // Set title
        if (currentProductUri == null) {
            // This is a new product
            getSupportActionBar().setTitle(getString(R.string.title_add));
        } else {
            // This is an existing product
            getSupportActionBar().setTitle(getString(R.string.title_editor));
            // Initialize a loader to read the data from the database and display the current
            // values in the activity
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }
        // Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Find all relevant views to read user input from
        editProductName = (EditText) findViewById(R.id.edit_product_name);
        editProductImage = (ImageView) findViewById(R.id.edit_product_image);
        addImage = (ImageView) findViewById(R.id.add_image);
        editProductPrice = (EditText) findViewById(R.id.edit_price);
        editProductQuantity = (TextView) findViewById(R.id.edit_quantity);
        editSupplierName = (EditText) findViewById(R.id.edit_supplier_name);
        editSupplierEmail = (EditText) findViewById(R.id.edit_supplier_email);
        editSupplierPhone = (EditText) findViewById(R.id.edit_supplier_phone);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonImageClick();
            }
        });

        // Set touchListeners to user input fields
        editProductName.setOnTouchListener(mTouchListener);
        editProductImage.setOnTouchListener(mTouchListener);
        addImage.setOnTouchListener(mTouchListener);
        editProductPrice.setOnTouchListener(mTouchListener);
        editProductQuantity.setOnTouchListener(mTouchListener);
        editSupplierName.setOnTouchListener(mTouchListener);
        editSupplierPhone.setOnTouchListener(mTouchListener);

        // Price formatter
        editProductPrice.setFilters(new InputFilter[]{
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 6, afterDecimal = 2;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = editProductPrice.getText() + source.toString();

                        if (temp.equals(".")) {
                            return "0.";
                        } else if (temp.toString().indexOf(".") == -1) { // no decimal point yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }

                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });

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
        if (cursor.moveToFirst()) {

            DatabaseUtils.dumpCursor(cursor);

            final String name = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_PRODUCT_NAME));
            editProductName.setText(name);

            String productImageString = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_PRODUCT_IMAGE));
            if (TextUtils.isEmpty(productImageString)) {
                editProductImage.setImageResource(R.drawable.product_image_placeholder);
            } else {
                imageUri = Uri.parse(productImageString);
                Picasso.with(this).load(imageUri).into(editProductImage);
                Picasso.with(this).load(imageUri).into(editProductImage);
            }

            double price = cursor.getDouble(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_PRODUCT_PRICE));
            String alma = Double.toString(price);
            editProductPrice.setText(Double.toString(price));

            final int quantity = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_PRODUCT_QUANTITY));
            editProductQuantity.setText(String.valueOf(quantity));

            editSupplierName.setText(cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_SUPPLIER_NAME)));

            final String email = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_SUPPLIER_EMAIL));
            editSupplierEmail.setText(email);

            editSupplierPhone.setText(cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry
                    .COLUMN_SUPPLIER_PHONE)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear all views
        editProductName.setText("");
        editProductImage.setImageResource(R.drawable.product_image_placeholder);
        editProductPrice.setText("");
        editProductQuantity.setText("");
        editSupplierName.setText("");
        editSupplierEmail.setText("");
        editSupplierPhone.setText("");
    }

    private int modifyQuantity(Uri itemUri, int quantityCount) {
        if (quantityCount < 0) {
            Toast.makeText(this, getString(R.string.toast_error_quantity),
                    Toast.LENGTH_SHORT).show();
            return 0;
        }
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityCount);
        int numRowsUpdated = getContentResolver().update(itemUri, values, null, null);
        return numRowsUpdated;
    }

    private void buttonImageClick() {
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            try {
                imageUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + imageUri.toString());

                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                editProductImage.setImageBitmap(getBitmapFromUri(imageUri));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = editProductImage.getWidth();
        int targetH = editProductImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    // Menu settings
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        if (intent != null) {
            // add clear top flag so it doesn't create new instance of parent.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to SAVE menu item clicked
            case R.id.action_save:
                if (currentProductUri == null) {
                    addProduct();
                } else {
                    updateProduct();
                }

                return true;

            // Respond to up navigation arrow clicked
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_message_unsaved);
                builder.setPositiveButton(R.string.dialog_reply_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.dialog_reply_keep_editing, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Keep editing" button, so dismiss the dialog and continue editing.
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message_unsaved);
        builder.setPositiveButton(R.string.dialog_reply_discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        });
        builder.setNegativeButton(R.string.dialog_reply_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog and continue editing.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void addProduct() {

        if (!getEditorInputs()) {
            return;
        }

        // if
        // set Toast - Please fill the required fields.

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, inputProductName);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, inputProductPrice);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageUri.toString());
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, inputProductQuantity);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME, inputSupplierName);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL, inputSupplierEmail);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE, inputSupplierPhone);

        currentProductUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (currentProductUri == null) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.toast_error_insert), Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, getString(R.string.toast_confirm_insert), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void updateProduct() {
        if (!getEditorInputs()) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, inputProductName);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, inputProductPrice);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageUri.toString());
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, inputProductQuantity);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME, inputSupplierName);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL, inputSupplierEmail);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE, inputSupplierPhone);

        int numRowsUpdated = getContentResolver().update(currentProductUri, values, null, null);

        // Display error message in Log if product stock fails to update
        if (!(numRowsUpdated > 0)) {
            Toast.makeText(this, getString(R.string.toast_error_update), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.toast_confirm_update), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    public boolean getEditorInputs() {

        inputProductName = editProductName.getText().toString().trim();
        String inputPriceString = editProductPrice.getText().toString().trim();
        String inputQuantityString = editProductQuantity.getText().toString().trim();
        inputSupplierName = editSupplierName.getText().toString().trim();
        inputSupplierEmail = editSupplierEmail.getText().toString().trim();
        inputSupplierPhone = editSupplierPhone.getText().toString().trim();

        if (imageUri == null) {
            editProductImage.requestFocus();
            Toast.makeText(EditorActivity.this, "Image is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (emptyInput(inputProductName)) {
            editProductName.requestFocus();
            editProductName.setError("Empty Product Name");
            return false;
        }

        if (emptyInput(inputPriceString)) {
            editProductPrice.requestFocus();
            editProductPrice.setError("Empty Product Price");
            return false;
        }

        if (inputQuantityString == "" || inputQuantityString.equals("0")) {
            editProductQuantity.requestFocus();
            editProductQuantity.setError("Quantity cannot be 0 or empty.");
            return false;
        }

        if (emptyInput(inputSupplierName)) {
            editSupplierName.requestFocus();
            editSupplierName.setError("Empty Supplier Name");
            return false;
        }

        if (emptyInput(inputSupplierEmail)) {
            editSupplierEmail.requestFocus();
            editSupplierEmail.setError("Empty Supplier Email");
            return false;
        }

        if (emptyInput(inputSupplierPhone)) {
            editSupplierPhone.requestFocus();
            editSupplierPhone.setError("Empty Supplier Phone");
            return false;
        }

        inputProductPrice = Double.valueOf(inputPriceString);
        inputProductQuantity = Integer.valueOf(inputQuantityString);

        return true;
    }

    /**
     * Method to check if input is empty or non-empty
     *
     * @param input
     * @return true if input in empty, false otherwise
     */
    public boolean emptyInput(String input) {
        if (input != null && !input.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Method to check if email is valid or invalid
     *
     * @param email
     * @return true if email is valid (abc@xyz.com), false otherwise
     */
    public boolean validateEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
