package com.jbeslinger.budgetbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class AccountsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        populateAccountsListView();
        setOnClickListViewItems();
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
            showAddAccountDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    //Presents the user with a DialogFragment for creating and adding a new account
    private void showAddAccountDialog() {
        FragmentManager manager = getSupportFragmentManager();
        AddAccountDialogFragment fragment = AddAccountDialogFragment.newInstance();
        fragment.show(manager, "fragment_add_account");
    }

    private void showModifyAccountDialog(int index) {
        FragmentManager manager = getSupportFragmentManager();
        ModifyAccountDialogFragment fragment = ModifyAccountDialogFragment.newInstance(index);
        fragment.show(manager, "fragment_modify_account");
    }

    //Takes all of the accounts and their balances and fills the ListView with them
    private void populateAccountsListView() {
        ListView listViewAccounts = (ListView)findViewById(R.id.listViewAccounts);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.big_list_item, UserManager.currentUser.getAccountNamesAndBalances());
        listViewAccounts.setAdapter(arrayAdapter);
    }

    //Sets OnItemClickListenevers to each of the ListView items
    //This is so that the user can modify and delete accounts
    private void setOnClickListViewItems() {
        ListView listViewAccounts = (ListView)findViewById(R.id.listViewAccounts);
        listViewAccounts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showModifyAccountDialog(position);
            }
        });
    }

}
