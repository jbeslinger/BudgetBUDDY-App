package com.jbeslinger.budgetbuddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_CATEGORY = "category";

    public DatabaseHelper(@Nullable Context context, @Nullable String databaseName) {
        super(context, databaseName, null, UserManager.currentUser.databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String accountName;
        for (BankAccount account : UserManager.currentUser.accounts) {
            accountName = account.name.replaceAll(" ", "_").toLowerCase();
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + accountName + " (" +
                            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            COLUMN_AMOUNT + " DECIMAL(13,2)," +
                            COLUMN_DATE + " DATE," +
                            COLUMN_NOTES + " TEXT," +
                            COLUMN_CATEGORY + " TEXT)"
            );
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

}
