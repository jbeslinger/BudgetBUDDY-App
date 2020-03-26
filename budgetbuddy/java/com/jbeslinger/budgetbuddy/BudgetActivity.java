package com.jbeslinger.budgetbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

public class BudgetActivity extends AppCompatActivity {

    private EditText editTextEstIncome;
    private TextView textViewRemainingIncome;
    private String[] keys; // Used to reference budget HashMap keys with an index number
    private double parsedAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        editTextEstIncome = (EditText)findViewById(R.id.editTextEstIncome);
        editTextEstIncome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            private String current = "";
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    editTextEstIncome.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[$,.]", "");
                    parsedAmount = Double.parseDouble(cleanString);
                    parsedAmount /= 100;
                    String formatted = NumberFormat.getCurrencyInstance().format((parsedAmount));
                    current = formatted;
                    editTextEstIncome.setText(formatted);
                    editTextEstIncome.setSelection(formatted.length());
                    editTextEstIncome.addTextChangedListener(this);
                }
                updateRemainingIncome();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        textViewRemainingIncome = (TextView)findViewById(R.id.textViewRemainingIncome);
        populateBudgetListView();
        setOnClickListViewItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_presets_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add) {
            showAddCategoryDialog();
        } else if (id == R.id.presets) {
            showPresetsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddCategoryDialog() {
        FragmentManager manager = getSupportFragmentManager();
        AddCategoryDialogFragment fragment = AddCategoryDialogFragment.newInstance();
        fragment.show(manager, "fragment_add_category");
    }

    private void showModifyAccountDialog(String key) {
        FragmentManager manager = getSupportFragmentManager();
        ModifyCategoryDialogFragment fragment = ModifyCategoryDialogFragment.newInstance(key);
        fragment.show(manager, "fragment_modify_category");
    }

    private void showPresetsDialog() {
        FragmentManager manager = getSupportFragmentManager();
        PresetsDialogFragment fragment = PresetsDialogFragment.newInstance();
        fragment.show(manager, "fragment_presets");
    }

    private void populateBudgetListView() {
        ListView listViewBudgetCategories = (ListView)findViewById(R.id.listViewBudgetCategories);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.big_list_item, UserManager.currentUser.getCategoriesAndLimits());
        listViewBudgetCategories.setAdapter(arrayAdapter);
        keys = UserManager.currentUser.getKeys();
    }

    private void setOnClickListViewItems() {
        ListView listViewBudgetCategories = (ListView)findViewById(R.id.listViewBudgetCategories);
        listViewBudgetCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showModifyAccountDialog(keys[position]);
            }
        });
    }

    private void updateRemainingIncome() {
        double budgetSum = 0.0, remainingAmount = 0.0;
        for (int index = 0; index < keys.length; index++) {
            budgetSum += UserManager.currentUser.budget.get(keys[index]);
        }
        remainingAmount = parsedAmount - budgetSum;
        String formatted = NumberFormat.getCurrencyInstance().format((remainingAmount));
        if (remainingAmount >= 0.0) {
            formatted = "+" + formatted;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textViewRemainingIncome.setTextColor(getColor(R.color.colorPositive));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textViewRemainingIncome.setTextColor(getColor(R.color.colorNegative));
            }
        }
        textViewRemainingIncome.setText(formatted);
    }

}
