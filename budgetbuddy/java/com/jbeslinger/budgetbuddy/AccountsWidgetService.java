package com.jbeslinger.budgetbuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccountsWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent i) {
        return new AccountsWidgetItemFactory(getApplicationContext(), i);
    }

}

class AccountsWidgetItemFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private List<String> accountsList = new ArrayList();

    public AccountsWidgetItemFactory(Context c, Intent i) {
        this.context = c;
    }

    @Override
    public void onCreate() { }

    @Override
    public void onDataSetChanged() {
        if (UserManager.currentUser == null) {
            loadDefaultUser(context);
        }
        accountsList = UserManager.currentUser.getAccountNamesAndBalances();
    }

    @Override
    public void onDestroy() { }

    @Override
    public int getCount() { return accountsList.size(); }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.big_list_item);
        views.setTextColor(R.id.textViewAccountData, Color.WHITE);
        views.setTextViewText(R.id.textViewAccountData, accountsList.get(position));
        return views;
    }

    @Override
    public RemoteViews getLoadingView() { return null; }

    @Override
    public int getViewTypeCount() { return 1; }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public boolean hasStableIds() { return true; }

    private void loadDefaultUser(final Context context) {
        try {
            UserManager.loadUserData(context, context.getString(R.string.key_defaultusername));
            Toast.makeText(context, "Welcome back, " + context.getString(R.string.key_defaultusername) + "!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String filepath = context.getFilesDir() + context.getString(R.string.key_defaultusername) + ".dat";
            File file = new File(filepath);
            if (file.exists()) {
                file.delete(); //Then delete the file
            }
            UserManager.createNewUser(context, context.getString(R.string.key_defaultusername)); //And create it again
            UserManager.currentUser = new User(context.getString(R.string.key_defaultusername));
            UserManager.saveUserData(context);
            Toast.makeText(context, "Generated user " +context.getString(R.string.key_defaultusername) + ".", Toast.LENGTH_SHORT).show();
        }
    }

}
