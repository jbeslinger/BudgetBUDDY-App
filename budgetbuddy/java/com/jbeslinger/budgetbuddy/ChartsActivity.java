package com.jbeslinger.budgetbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class ChartsActivity extends AppCompatActivity {

    private DatabaseManager databaseManager = new DatabaseManager(this);
    private Spinner spinnerAccounts;
    private PieChartView pieChartViewSpending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        spinnerAccounts = (Spinner)findViewById(R.id.spinnerAccounts);
        fillSpinner();
        pieChartViewSpending = (PieChartView)findViewById(R.id.pieChartViewSpending);
        spinnerAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    setupChart(position);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void fillSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.big_list_item, UserManager.currentUser.getAccountNames());
        spinnerAccounts.setAdapter(arrayAdapter);
    }

    private void setupChart(int accountIndex) {
        List<SliceValue> pieData = new ArrayList<>();
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
                if (category.equals("Income")) { ++position; continue; }
                index = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT);
                double amount = Double.parseDouble(cursor.getString(index));
                if (categories.containsKey(category)) {
                    double runningTotal = 0.0;
                    runningTotal = categories.get(category);
                    runningTotal += amount;
                    categories.remove(category);
                    categories.put(category, runningTotal);
                } else {
                    categories.put(category, amount);
                }
                ++position;
            }
        }

        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
            pieData.add(new SliceValue((float) value, randomColor()).setLabel(key + " - " + numberFormat.format(Math.abs(value))));
        }

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true);
        pieChartViewSpending.setPieChartData(pieChartData);
    }

    private int randomColor() {
        Random r = new Random();
        int red = r.nextInt(256);
        int green = r.nextInt(256);
        int blue = r.nextInt(256);
        return Color.rgb(red, green, blue);
    }

}
