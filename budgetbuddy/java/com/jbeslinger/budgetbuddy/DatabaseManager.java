package com.jbeslinger.budgetbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.security.spec.ECField;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatabaseManager {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DatabaseManager(Context c) {
        context = c;
    }

    public DatabaseManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context, context.getFilesDir() + "\\" + UserManager.currentUser.username + ".db");
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void addTransaction(BankAccount account, double amount, String notes, String category) {
        String tableName = account.name.replace(" ", "_").toLowerCase();
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.COLUMN_AMOUNT, amount);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date today = Calendar.getInstance().getTime();
        contentValue.put(DatabaseHelper.COLUMN_DATE, format.format(today));

        contentValue.put(DatabaseHelper.COLUMN_NOTES, notes);
        contentValue.put(DatabaseHelper.COLUMN_CATEGORY, category);
        database.insert(tableName, null, contentValue);

        account.balance += amount;
    }

    public void addTransaction(BankAccount account, double amount, Date date, String notes, String category) {
        String tableName = account.name.replace(" ", "_").toLowerCase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AMOUNT, amount);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        values.put(DatabaseHelper.COLUMN_DATE, format.format(date));

        values.put(DatabaseHelper.COLUMN_NOTES, notes);
        values.put(DatabaseHelper.COLUMN_CATEGORY, category);
        database.insert(tableName, null, values);

        account.balance += amount;
    }

    public Cursor getTransactionData(BankAccount account, Date startDate, Date endDate, String[] columns) {
        String tableName = account.name.replace(" ", "_").toLowerCase();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String selection = "";
        for (int i = 0; i < columns.length; i++) {
            selection += columns[i];
            if (i+1 != columns.length)
                selection += ", ";
        }
        String query = "SELECT " + selection + " FROM " + tableName + " WHERE " + DatabaseHelper.COLUMN_DATE + " BETWEEN '" + format.format(startDate) + "' AND '" + format.format(endDate) +
                "' ORDER BY " + DatabaseHelper.COLUMN_DATE + " DESC, " + DatabaseHelper.COLUMN_ID + " DESC";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) { cursor.moveToFirst(); }
        return cursor;
    }

    public Cursor getTransactionAmounts(BankAccount account, Date startDate, Date endDate) {
        String tableName = account.name.replace(" ", "_").toLowerCase();
        String[] columns = new String[]{ DatabaseHelper.COLUMN_AMOUNT, DatabaseHelper.COLUMN_CATEGORY };
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String query = "SELECT " + columns[0] + "," + columns[1] + " FROM " + tableName + " WHERE " + DatabaseHelper.COLUMN_DATE + " BETWEEN '" + format.format(startDate) + "' AND '" + format.format(endDate) +
                "' ORDER BY " + DatabaseHelper.COLUMN_DATE + " DESC, " + DatabaseHelper.COLUMN_ID + " DESC";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) { cursor.moveToFirst(); }
        return cursor;
    }

    public Cursor getTransactionByID(BankAccount account, long id) {
        String tableName = account.name.replace(" ", "_").toLowerCase();
        String[] columns = new String[]{ DatabaseHelper.COLUMN_DATE, DatabaseHelper.COLUMN_AMOUNT, DatabaseHelper.COLUMN_NOTES, DatabaseHelper.COLUMN_CATEGORY };
        String query = "SELECT " + columns[0] + "," + columns[1] + ","  + columns[2] + "," + columns[3] + " FROM " + tableName + " WHERE " + DatabaseHelper.COLUMN_ID + "=" + id;
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) { cursor.moveToFirst(); }
        return cursor;
    }

    public int updateTransaction(BankAccount account, long id, double newAmount, Date date, String notes, String category) {
        String tableName = account.name.replace(" ", "_").toLowerCase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AMOUNT, newAmount);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        values.put(DatabaseHelper.COLUMN_DATE, format.format(date));
        values.put(DatabaseHelper.COLUMN_NOTES, notes);
        values.put(DatabaseHelper.COLUMN_CATEGORY, category);

        String query = "SELECT " + DatabaseHelper.COLUMN_AMOUNT + " FROM " + tableName + " WHERE " + DatabaseHelper.COLUMN_ID + "=" + id;
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) { cursor.moveToFirst(); }
        double oldAmount = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT));
        double difference = oldAmount - newAmount;
        account.balance -= difference;

        int i = database.update(tableName, values, DatabaseHelper.COLUMN_ID + " = " + id, null);
        return i;
    }

    public void deleteTransaction(BankAccount account, long id) {
        String tableName = account.name.replace(" ", "_").toLowerCase();
        String query = "SELECT " + DatabaseHelper.COLUMN_AMOUNT + " FROM " + tableName + " WHERE " + DatabaseHelper.COLUMN_ID + "=" + id;
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) { cursor.moveToFirst(); }
        int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT);
        double amount = cursor.getDouble(columnIndex);
        database.delete(tableName, DatabaseHelper.COLUMN_ID + "=" + id, null);
        account.balance += -(amount);
    }

    public void updateTables() {
        dbHelper.onUpgrade(database, UserManager.currentUser.databaseVersion, UserManager.currentUser.databaseVersion++);
        UserManager.currentUser.databaseVersion++;
    }

    public void renameTable(BankAccount account, String newTableName) {
        String oldTableName = account.name.replace(" ", "_").toLowerCase();
        newTableName = newTableName.replace(" ", "_").toLowerCase();
        try {
            database.execSQL("ALTER TABLE " + oldTableName + " RENAME TO " + newTableName);
        } catch (Exception e) {
            return;
        }
    }

    public void dropTable(BankAccount account) {
        String tableName = account.name.replace(" ", "_").toLowerCase();
        database.execSQL("DROP TABLE IF EXISTS " + tableName);
    }

}
