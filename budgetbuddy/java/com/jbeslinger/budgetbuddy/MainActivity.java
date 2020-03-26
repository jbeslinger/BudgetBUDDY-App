package com.jbeslinger.budgetbuddy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private DatabaseManager databaseManager = new DatabaseManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UserManager.currentUser == null) {
            loadDefaultUser(this);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        populateAccountsListView();
        setClickListeners(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateAccountsListView();
    }

    //Goes through all the buttons and defines the OnClickListeners for each of them
    private void setClickListeners(final Context context) {
        ImageButton imageButton = (ImageButton)findViewById(R.id.buttonAccounts); //Accounts Activity
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AccountsActivity.class);
                startActivity(intent);
            }
        });
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, getString(R.string.activity_name_accounts), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imageButton = (ImageButton)findViewById(R.id.buttonBudget);//Budget Activity
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BudgetActivity.class);
                startActivity(intent);
            }
        });
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, getString(R.string.activity_name_budget), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imageButton = (ImageButton)findViewById(R.id.buttonCharts);//Charts Activity
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChartsActivity.class);
                startActivity(intent);
            }
        });
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, getString(R.string.activity_name_charts), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imageButton = (ImageButton)findViewById(R.id.buttonBars);//Bars Activity
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BarsActivity.class);
                startActivity(intent);
            }
        });
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, getString(R.string.activity_name_bars), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imageButton = (ImageButton)findViewById(R.id.buttonTransactions);//Transactions Activity
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TransactionsActivity.class);
                startActivity(intent);
            }
        });
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, getString(R.string.activity_name_transactions), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imageButton = (ImageButton)findViewById(R.id.buttonSettings);//Settings Activity
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "To be implemented.", Toast.LENGTH_SHORT).show();
                /*Intent intent = new Intent(context, SettingsActivity.class);
                startActivity(intent);*/
            }
        });
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, getString(R.string.activity_name_settings), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
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

    //Takes all of the accounts and their balances and fills the ListView with them
    private void populateAccountsListView() {
        ListView listViewAccounts = (ListView)findViewById(R.id.listViewAccounts);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.big_list_item, UserManager.currentUser.getAccountNamesAndBalances());
        listViewAccounts.setAdapter(arrayAdapter);
    }

}
