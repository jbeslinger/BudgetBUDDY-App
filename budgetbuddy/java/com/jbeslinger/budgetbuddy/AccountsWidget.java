package com.jbeslinger.budgetbuddy;

import android.app.LauncherActivity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccountsWidget extends AppWidgetProvider {

    private static final String UPDATE_ACCOUNTS_ACTION = "com.jbeslinger.budgetbuddy.updateaccounts";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, AccountsWidget.class);

        // Obtain all instances of our widget
        // Remember that you can have as many instances of the same widget as you want on the home screen
        int[] allWidgetInstancesIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetInstancesIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_accounts);

            Intent intent = new Intent(context, AccountsWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            remoteViews.setRemoteAdapter(R.id.listViewAccounts, intent);

            // Create an intent that when received will launch the PopUpActivity
            intent = new Intent(context, AccountsWidget.class);
            intent.setAction(UPDATE_ACCOUNTS_ACTION);
            PendingIntent refreshIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Set up the onClickListener of the widget
            // Now, when the widget is pressed the pendingIntent will be sent
            remoteViews.setOnClickPendingIntent(R.id.imageButtonRefresh, refreshIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(UPDATE_ACCOUNTS_ACTION)) {
            Toast.makeText(context, "Refreshed", Toast.LENGTH_SHORT).show();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, AccountsWidget.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewAccounts);
        }
        super.onReceive(context, intent);
    }

}