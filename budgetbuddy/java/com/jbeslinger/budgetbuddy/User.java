package com.jbeslinger.budgetbuddy;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class User implements Serializable {

    public final static HashMap<String, Double> defaultBudget = new HashMap<String, Double>() {
        {
            put("Car", 00.00);
            put("Entertainment", 00.00);
            put("Fast Food", 00.00);
            put("Gasoline", 00.00);
            put("Gifts", 00.00);
            put("Groceries", 00.00);
            put("Medical", 00.00);
            put("House", 00.00);
            put("Insurance", 00.00);
            put("Internet", 00.00);
            put("Phone", 00.00);
            put("Savings", 00.00);
            put("Taxes", 00.00);
            put("Utilities", 00.00);
        }
    };

    String username;
    List<BankAccount> accounts = new ArrayList<>();
    HashMap<String, Double> budget;
    HashMap<String, HashMap> presets = new HashMap<>();
    int databaseVersion = 1;

    public User(String username) {
        this.username = username;
        this.accounts.add(new BankAccount("Edgar County Bank", 0.00));
        this.accounts.add(new BankAccount("American Express Personal Savings", 0.00));

        this.budget = defaultBudget;
    }

    //Returns a list of user's bank account names and formatted balances in each entry
    public List<String> getAccountNamesAndBalances() {
        ArrayList<String> list = new ArrayList<>();
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        for (BankAccount account : this.accounts) {
            list.add(account.name + "\n" + format.format(account.balance));
        }
        return list;
    }

    public List<String> getAccountNames() {
        ArrayList<String> list = new ArrayList<>();
        for (BankAccount account : this.accounts) {
            list.add(account.name);
        }
        return list;
    }

    //This returns a true if the accounts ArrayList contains a BankAccount of the same name
    //Used to stop user from having two BankAccounts of the same name
    public boolean containsAccount(String accountName) {
        List<String> names = new ArrayList<>();
        for (BankAccount account : this.accounts) {
            names.add(account.name);
        }
        if (names.contains(accountName)) {
            return true;
        } else {
            return false;
        }
    }

    public void addAccount(String name, double balance) {
        this.accounts.add(new BankAccount(name, balance));
    }

    public void modifyAccount(int index, String newName, double newBalance) {
        BankAccount a = this.accounts.get(index);
        a.name = newName;
        a.balance = newBalance;
    }

    public void removeAccount(int index) {
        accounts.remove(index);
    }

    //Returns a sorted String List of human-readable data representing the User's budget
    public List<String> getCategoriesAndLimits() {
        ArrayList<String> list = new ArrayList<>();
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        for (Map.Entry<String, Double> entry : this.budget.entrySet()) { //Iterate through the entire HashMap and add the entries to list
            String key = entry.getKey();
            double value = entry.getValue();
            list.add(key + " -\n" + format.format(value));
        }
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    //Returns a sorted String List of category names including "Income" so the user can pick from them
    public List<String> getCategories() {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, Double> entry : this.budget.entrySet()) {
            String key = entry.getKey();
            list.add(key);
        }
        list.add("Income");
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    //Returns a String array of all keys in the HashMap
    public String[] getKeys() {
        int index = 0;
        String[] keys = new String[this.budget.size()];
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, Double> entry : this.budget.entrySet()) { //Iterate through the entire HashMap and add the entries to list
            list.add(entry.getKey());
        }
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        for (String s : list) {
            keys[index] = s;
            ++index;
        }
        return keys;
    }

    public boolean addCategory(String category, double limit) {
        if (this.budget.containsKey(category)) {
            return false;
        }

        this.budget.put(category, limit);
        return true;
    }

    public boolean removeCategory(String category) {
        if (this.budget.size() == 1) {
            return false;
        }

        this.budget.remove(category);
        return true;
    }

    public boolean updateBudgetAmount(String oldCategory, String newCategory, double newAmount) {
        if (this.budget.containsKey(newCategory)) {
            return false;
        }

        this.budget.remove(oldCategory);
        this.budget.put(newCategory, newAmount);
        return true;
    }

    @SuppressLint("NewApi")
    public void updateBudgetAmount(String key, double newAmount) {
        this.budget.replace(key, newAmount);
    }

    //Returns all of the keys in the presets HashMap plus an entry for Default and SAVE PRESET
    public List<String> getPresetNames() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Default");
        for (Map.Entry<String, HashMap> entry : this.presets.entrySet()) { //Iterate through the entire HashMap and add the entries to list
            list.add(entry.getKey());
        }
        list.add("– SAVE PRESET –");
        return list;
    }

    // Changes the budget to the preset requested
    public void changeBudget(String presetName) {
        this.budget.clear();
        switch (presetName) {
            case "Default":
                this.budget.putAll(defaultBudget);
                break;
            default:
                this.budget.putAll(this.presets.get(presetName));
                break;
        }
    }

    public void addPreset() {
        HashMap<String, Double> presetBudget = new HashMap<>();
        presetBudget.putAll(this.budget);
        String presetName = "Untitled (";
        int number = 0;
        boolean trigger = false;

        while (!trigger) {
            if (this.presets.containsKey(presetName + number + ")")) {
                ++number;
            } else {
                this.presets.put(presetName + number + ")", presetBudget);
                trigger = true;
            }
        }
    }

    public void removePreset(String presetName) {
        this.presets.remove(presetName);
    }

    // Used to rename a preset. Removes the preset from the presets Hashmap and re-adds it under the new key.
    public void renamePreset(String presetToRename, String newName) {
        HashMap<String, Double> presetTempHolder = new HashMap<>();
        presetTempHolder.putAll(this.presets.get(presetToRename));
        removePreset(presetToRename);
        this.presets.put(newName, presetTempHolder);
    }

}
