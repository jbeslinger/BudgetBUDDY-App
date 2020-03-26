package com.jbeslinger.budgetbuddy;

import android.content.Context;
import android.database.Cursor;

import java.io.Serializable;

class BankAccount implements Serializable {

    String name;
    double balance;

    public BankAccount(String accountName, double startingBalance) {
        this.name = accountName;
        this.balance = startingBalance;
    }

}
