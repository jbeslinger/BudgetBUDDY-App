package com.jbeslinger.budgetbuddy;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;

public class TransactionCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;
    private int lightColor, darkColor; //Alternate between these two background colors for each row
    private int positiveColor, negativeColor;
    private boolean alternate = false;

    public TransactionCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        lightColor = context.getResources().getColor(R.color.colorBackgroundWhite);
        darkColor = context.getResources().getColor(R.color.colorBackgroundGrey);
        positiveColor  = context.getResources().getColor(R.color.colorPositive);
        negativeColor = context.getResources().getColor(R.color.colorNegative);
        cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //Inflates the view and returns it
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.transaction_list_item, parent, false);
    }

    //Sets the elements of the view from newView
    public void bindView(View view, Context context, Cursor cursor) {
        LinearLayout linearLayoutParent = (LinearLayout)view.findViewById(R.id.parent);
        if (alternate) {
            linearLayoutParent.setBackgroundColor(darkColor);
        } else {
            linearLayoutParent.setBackgroundColor(lightColor);
        }
        alternate = !alternate;

        TextView textView = (TextView)view.findViewById(R.id.textViewTransID);
        textView.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));

        textView = (TextView)view.findViewById(R.id.textViewTransAmount);
        double parsedAmount =  Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT)));
        String amount;
        if (parsedAmount >= 0) {
            amount = "+" + NumberFormat.getCurrencyInstance().format(parsedAmount);
            textView.setTextColor(positiveColor);
        } else {
            amount = NumberFormat.getCurrencyInstance().format(parsedAmount);
            textView.setTextColor(negativeColor);
        }
        textView.setText(amount);

        textView = (TextView)view.findViewById(R.id.textViewTransDate);
        textView.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE)));
        textView = (TextView)view.findViewById(R.id.textViewTransCategory);
        textView.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY)));
    }

}
