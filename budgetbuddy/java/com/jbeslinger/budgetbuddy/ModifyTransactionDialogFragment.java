package com.jbeslinger.budgetbuddy;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.DialogFragment;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ModifyTransactionDialogFragment extends DialogFragment {

    private EditText editTextAmount, editTextNotes;
    private DatePicker datePickerDate;
    private Spinner spinnerCategory;
    private ImageButton buttonUpdate, buttonDelete;
    private double parsedBalance;
    private DatabaseManager databaseManager;

    private ModifyTransactionDialogFragment () {};

    public static ModifyTransactionDialogFragment newInstance(int accountIndex, int transactionID) {
        ModifyTransactionDialogFragment fragment = new ModifyTransactionDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", "Modify Transaction");
        args.putInt("account index", accountIndex);
        args.putInt("transaction id", transactionID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modify_transaction, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        databaseManager = new DatabaseManager(getContext());

        super.onViewCreated(view, savedInstanceState);

        final int accountIndex = getArguments().getInt("account index"),
                transactionID = getArguments().getInt("transaction id");

        databaseManager.open();
        Cursor cursor = databaseManager.getTransactionByID(UserManager.currentUser.accounts.get(accountIndex), transactionID);
        databaseManager.close();

        editTextAmount = (EditText)view.findViewById(R.id.editTextAmount);
        double previousAmount = Math.abs(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT)));
        String s = NumberFormat.getCurrencyInstance().format(previousAmount);
        editTextAmount.setText(s);
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            private String current = "";
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    editTextAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[$,.]", "");
                    parsedBalance = Double.parseDouble(cleanString);
                    parsedBalance /= 100;
                    String formatted = NumberFormat.getCurrencyInstance().format((parsedBalance));
                    current = formatted;
                    editTextAmount.setText(formatted);
                    editTextAmount.setSelection(formatted.length());
                    editTextAmount.addTextChangedListener(this);
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        editTextNotes = (EditText)view.findViewById(R.id.editTextNotes);
        editTextNotes.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTES)));

        datePickerDate = (DatePicker)view.findViewById(R.id.datePickerDate);
        Calendar cal = Calendar.getInstance();
        Date d = Calendar.getInstance().getTime();
        try { d = new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE))); } catch (ParseException e) { e.printStackTrace(); }
        cal.setTime(d);
        datePickerDate.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        spinnerCategory = (Spinner)view.findViewById(R.id.spinnerCategory);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.small_list_item, UserManager.currentUser.getCategories());
        spinnerCategory.setAdapter(arrayAdapter);
        spinnerCategory.setSelection(arrayAdapter.getPosition(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY))));

        buttonUpdate = (ImageButton) view.findViewById(R.id.buttonRename);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = datePickerDate.getDayOfMonth(); int month = datePickerDate.getMonth(); int year =  datePickerDate.getYear();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                Date datePicked = calendar.getTime();

                if (editTextAmount.getText().toString().isEmpty()) {
                    parsedBalance = 0.0;
                }
                if (!spinnerCategory.getSelectedItem().toString().equals("Income")) { //If the balance to be entered is NOT Income, then invert the amount
                    parsedBalance *= -1.0;
                }

                try {
                    databaseManager.open();
                    databaseManager.updateTransaction(UserManager.currentUser.accounts.get(accountIndex), transactionID, parsedBalance, datePicked, editTextNotes.getText().toString(), spinnerCategory.getSelectedItem().toString());
                    checkLimitBreak(accountIndex, spinnerCategory.getSelectedItem().toString());
                    databaseManager.close();
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Toast.makeText(getContext(), "accountIndex: " + accountIndex + ", transactionID: " + transactionID, Toast.LENGTH_LONG).show();
                }

                dismiss();
            }
        });

        buttonDelete = (ImageButton)view.findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseManager.open();
                databaseManager.deleteTransaction(UserManager.currentUser.accounts.get(accountIndex), transactionID);
                databaseManager.close();
                dismiss();
            }
        });

        String title = getArguments().getString("title", "Untitled");
        getDialog().setTitle(title);
    }

    //This method will compare the transaction being added to the specified budget limit and throw a notification if the transaction goes over the limit
    private void checkLimitBreak(int accountIndex, String categoryName) {
        if (categoryName.equals("Income")) {
            return;
        }

        Date beginningOfThisMonth, today = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        beginningOfThisMonth = calendar.getTime();
        Cursor transactionAmounts = databaseManager.getTransactionAmounts(UserManager.currentUser.accounts.get(accountIndex), beginningOfThisMonth, today);

        double limit = UserManager.currentUser.budget.get(categoryName);
        double sum = 0.0;

        if (transactionAmounts == null) {
            Toast.makeText(getContext(), "Query returned nothing", Toast.LENGTH_SHORT).show();
            return;
        } else {
            int position = 0;
            while (transactionAmounts.moveToPosition(position)) {
                int index = transactionAmounts.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY);
                String categoryToCompare = transactionAmounts.getString(index);

                if (categoryToCompare.equals(categoryName)) {
                    index = transactionAmounts.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT);
                    sum += Math.abs(Double.parseDouble(transactionAmounts.getString(index)));
                }

                ++position;
            }
        }

        if (sum > limit) {
            issueNotification(categoryName);
        }
    }

    // Vibrate and then build the notification
    private void issueNotification(String overbudgetCategoryName) {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 250 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));
        } else { v.vibrate(250); }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_ape_notification);
        builder.setContentTitle("You went overbudget in the " + overbudgetCategoryName + " category");
        builder.setContentText("Touch to go to Bar view.");

        Intent intent = new Intent(getContext(), BarsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addParentStack(BarsActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        UserManager.saveUserData(getContext());
        getActivity().recreate();
        super.onDismiss(dialog);
    }
}
