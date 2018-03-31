package com.example.android.a11_trackmystock;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.a11_trackmystock.data.ProductContract;
import com.squareup.picasso.Picasso;

import static android.R.attr.id;

/**
 * Created by koszojudit on 2017. 07. 20..
 */

public class ProductCursorAdapter extends CursorAdapter {

    // private static final Uri DUMMY_PICTURE_URI = Uri.parse("android.resource://com.example.android.a11_trackmystock/drawable/ic_launcher;\n");

    // Constructor for the adapter
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    // Class which describes how a list item is created
    public static class ProductViewHolder {

        ImageView listImage;
        TextView listName;
        TextView listPrice;
        TextView listQuantity;
        Button buttonSold;

        public ProductViewHolder(View itemView) {
            listName = (TextView) itemView.findViewById(R.id.list_product_name);
            listImage = (ImageView) itemView.findViewById(R.id.list_product_image);
            listPrice = (TextView) itemView.findViewById(R.id.list_product_price);
            listQuantity = (TextView) itemView.findViewById(R.id.list_quantity);
            buttonSold = (Button) itemView.findViewById(R.id.button_sold);
        }
    }

    // Inflate a new blank list item - no data is set (or bound) to the views yet
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent ,false);
        ProductViewHolder holder = new ProductViewHolder(view);
        view.setTag(holder);
        return view;
    }

    // This method binds the data in the current row pointed to by cursor to the given
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        ProductViewHolder holder = (ProductViewHolder)view.getTag();

        // Set data to respective views within list
        // Find the columns of attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the attributes from the Cursor for the current product
        final int productId = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        String imageString = cursor.getString(imageColumnIndex);
        double price = cursor.getDouble(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current item
        holder.listName.setText(productName);
        holder.listPrice.setText(Double.toString(price) + "  €");
        holder.listQuantity.setText(Integer.toString(quantity));
        // Use dummy picture as a placeholder when there is no image provided
        if (imageString == null || imageString.equals("")) {
            holder.listImage.setImageResource(R.drawable.empty_view);
        } else {
            Uri imageUri = Uri.parse(imageString);
            Picasso.with(context).load(imageUri).into(holder.listImage);
        }


        // Adjust quantity by -1 when click the 'Sold 1' Button
        holder.buttonSold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity == 0) {
                    // Avoid invalid stocks
                    Toast.makeText(context, context.getString(R.string.toast_error_quantity),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Uri productUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, productId);
                    // Create a new map of values, where column named 'quantity' is the key to update
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);
                    int rowsUpdated = context.getContentResolver().update(
                            productUri,         // the content URI
                            values,             // the columns to update
                            null,               // the column to select on
                            null                // the value to compare to
                    );
                    // Show a toast message if the update was unsuccessful
                    if (!(rowsUpdated > 0)) {
                        // If there are no rows updated, then there was an error with the update.
                        Toast.makeText(context, context.getString(R.string.toast_error_update),
                                Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
    }
}


