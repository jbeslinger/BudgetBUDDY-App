package com.jbeslinger.budgetbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TransactionsActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private ListView listViewTransactions;
    private Spinner spinnerAccounts;
    private Button buttonStartDate, buttonEndDate;

    private static Date firstOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstOfMonth = calendar.getTime();
        return firstOfMonth;
    }
    public static Date startDate = firstOfMonth();
    public static Date endDate = Calendar.getInstance().getTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        databaseManager = new DatabaseManager(this);
        spinnerAccounts = (Spinner)findViewById(R.id.spinnerAccounts);
        spinnerAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillListView();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        listViewTransactions = (ListView)findViewById(R.id.listViewTransactions);
        buttonStartDate = (Button)findViewById(R.id.buttonStartDate);
        buttonStartDate.setText("START DATE\n" + new SimpleDateFormat("MM/dd/yyyy").format(startDate));
        buttonEndDate = (Button)findViewById(R.id.buttonEndDate);
        buttonEndDate.setText("END DATE\n" + new SimpleDateFormat("MM/dd/yyyy").format(endDate));
        fillSpinner();
        fillListView();
        setOnClickListViewItems();
        setOnClickButtons();
    }

    private void showAddTransactionDialog() {
        FragmentManager manager = getSupportFragmentManager();
        AddTransactionDialogFragment fragment = AddTransactionDialogFragment.newInstance(spinnerAccounts.getSelectedItemPosition());
        fragment.show(manager, "fragment_add_transaction");
    }

    private void showModifyTransactionDialog(int transactionID) {
        FragmentManager manager = getSupportFragmentManager();
        ModifyTransactionDialogFragment fragment = ModifyTransactionDialogFragment.newInstance(spinnerAccounts.getSelectedItemPosition(), transactionID);
        fragment.show(manager, "fragment_modify_transaction");
    }

    private void showChangeDateDialog(int buttonID) {
        FragmentManager manager = getSupportFragmentManager();
        ChangeDateDialogFragment fragment = ChangeDateDialogFragment.newInstance(buttonID);
        fragment.show(manager, "fragment_change_date");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add) {
            showAddTransactionDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.big_list_item, UserManager.currentUser.getAccountNames());
        spinnerAccounts.setAdapter(arrayAdapter);
    }

    private void fillListView() {
        listViewTransactions.setAdapter(null);
        String[] columns = new String[] { DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_AMOUNT, DatabaseHelper.COLUMN_DATE, DatabaseHelper.COLUMN_CATEGORY };
        databaseManager.open();
        Cursor cursor = databaseManager.getTransactionData(UserManager.currentUser.accounts.get(spinnerAccounts.getSelectedItemPosition()), startDate, endDate, columns);
        databaseManager.close();

        TransactionCursorAdapter adapter = new TransactionCursorAdapter(this, cursor, 0);
        listViewTransactions.setAdapter(adapter);
    }

    private void setOnClickListViewItems() {
        listViewTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textViewID = (TextView)view.findViewById(R.id.textViewTransID);
                int transactionID = Integer.parseInt(textViewID.getText().toString());
                showModifyTransactionDialog(transactionID);
            }
        });
    }

    private void setOnClickButtons() {
        buttonStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeDateDialog(buttonStartDate.getId());
            }
        });
        buttonEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeDateDialog(buttonEndDate.getId());
            }
        });
    }

}
