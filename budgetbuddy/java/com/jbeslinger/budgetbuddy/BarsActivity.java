package com.jbeslinger.budgetbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BarsActivity extends AppCompatActivity {

    private DatabaseManager databaseManager = new DatabaseManager(this);
    private LinearLayout linearLayoutBars;
    private Spinner spinnerAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UserManager.currentUser == null) {
            loadDefaultUser(this);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bars);

        linearLayoutBars = (LinearLayout)findViewById(R.id.linearLayoutBars);
        spinnerAccounts = (Spinner)findViewById(R.id.spinnerAccounts);
        fillSpinner();
        spinnerAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    populateBarsListView(position);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void populateBarsListView(int accountIndex) {
        linearLayoutBars.removeAllViews();
        BankAccount account = UserManager.currentUser.accounts.get(accountIndex);
        HashMap<String, Double> categories = new HashMap<>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date today = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstOfMonth = calendar.getTime();

        databaseManager.open();
        Cursor cursor = databaseManager.getTransactionAmounts(account, firstOfMonth, today);
        databaseManager.close();

        if (cursor == null) {
            Toast.makeText(this, "Query returned nothing", Toast.LENGTH_SHORT).show();
        } else {
            int position = 0;
            while (cursor.moveToPosition(position)) {
                int index = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY);
                String category = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT);
                double amount = Double.parseDouble(cursor.getString(index));
                if (categories.containsKey(category)) {
                    double runningTotal = 0.0;
                    runningTotal = categories.get(category);
                    runningTotal += Math.abs(amount);
                    categories.remove(category);
                    categories.put(category, runningTotal);
                } else {
                    categories.put(category, Math.abs(amount));
                }
                ++position;
            }
        }

        HashMap<String, Double> usersBudget = UserManager.currentUser.budget;
        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();

            if (usersBudget.containsKey(key)) {
                View view = getLayoutInflater().inflate(R.layout.progress_bar_list_item, linearLayoutBars, false);
                linearLayoutBars.addView(view);
                TextView textViewCategoryName = (TextView) view.findViewById(R.id.textViewCategoryName);
                textViewCategoryName.setText(key);
                ProgressBar progressBarLimit = (ProgressBar) view.findViewById(R.id.progressBarLimit);
                double limit = usersBudget.get(key);
                progressBarLimit.setMax((int)limit);
                TextView textViewLimitAmount = (TextView) view.findViewById(R.id.textViewLimitAmount);
                textViewLimitAmount.setText("$" + (int)limit);
                TextView textViewAmountSpent = (TextView) view.findViewById(R.id.textViewAmountSpent);
                textViewAmountSpent.setText("$" + (int)value);
                if (value >= 0)
                    progressBarLimit.setProgress((int)value);
                else
                    progressBarLimit.setProgress(progressBarLimit.getMax());
            } else {

            }
        }

    }

    private void fillSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.big_list_item, UserManager.currentUser.getAccountNames());
        spinnerAccounts.setAdapter(arrayAdapter);
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

}
