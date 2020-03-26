package com.jbeslinger.budgetbuddy;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

public class QuickTransactionPopUpActivity extends Activity {

    private DatabaseManager databaseManager = new DatabaseManager(this);

    private ImageButton buttonAdd;
    private Spinner spinnerAccounts, spinnerCategory;
    private EditText editTextAmount, editTextNotes;

    private double parsedBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UserManager.currentUser == null) {
            loadDefaultUser(this);
        }

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_quick_transaction_pop_up);

        spinnerAccounts = (Spinner) findViewById(R.id.spinnerAccounts);
        populateAccountsSpinner();

        spinnerCategory = (Spinner) findViewById(R.id.spinnerCategory);
        populateCategoriesSpinner();

        editTextAmount = (EditText) findViewById(R.id.editTextAmount);
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

        editTextNotes = (EditText) findViewById(R.id.editTextNotes);

        buttonAdd = (ImageButton) findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextAmount.getText().toString().isEmpty()) {
                    parsedBalance = 0.0;
                }
                if (!spinnerCategory.getSelectedItem().toString().equals("Income")) { //If the balance to be entered is NOT Income, then invert the amount
                    parsedBalance *= -1.0;
                }

                databaseManager.open();
                databaseManager.addTransaction(UserManager.currentUser.accounts.get(spinnerAccounts.getSelectedItemPosition()), parsedBalance, editTextNotes.getText().toString(), spinnerCategory.getSelectedItem().toString());
                checkLimitBreak(spinnerAccounts.getSelectedItemPosition(), spinnerCategory.getSelectedItem().toString());
                databaseManager.close();

                QuickTransactionPopUpActivity.this.finish();
            }
        });
    }

    private void populateAccountsSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.small_list_item, UserManager.currentUser.getAccountNames());
        spinnerAccounts.setAdapter(arrayAdapter);
    }

    private void populateCategoriesSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.small_list_item, UserManager.currentUser.getCategories());
        spinnerCategory.setAdapter(arrayAdapter);
    }

    private void loadDefaultUser(final Context context) {
        try {
            UserManager.loadUserData(context, getString(R.string.key_defaultusername));
            Toast.makeText(context, "Welcome back, " + getString(R.string.key_defaultusername) + "!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String filepath = getFilesDir() + getString(R.string.key_defaultusername) + ".dat";
            File file = new File(filepath);
            if (file.exists()) {
                file.delete(); //Then delete the file
            }
            UserManager.createNewUser(context, getString(R.string.key_defaultusername)); //And create it again
            UserManager.currentUser = new User(getString(R.string.key_defaultusername));
            UserManager.saveUserData(context);
            Toast.makeText(context, "Generated user " + getString(R.string.key_defaultusername) + ".", Toast.LENGTH_SHORT).show();
        }
        databaseManager.open();
        databaseManager.close();
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
            Toast.makeText(getApplicationContext(), "Query returned nothing", Toast.LENGTH_SHORT).show();
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
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 250 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else { v.vibrate(500); }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_ape_notification);
        builder.setContentTitle("You went overbudget in the " + overbudgetCategoryName + " category");
        builder.setContentText("Touch to go to Bar view.");

        Intent intent = new Intent(getApplicationContext(), BarsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(BarsActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

}
