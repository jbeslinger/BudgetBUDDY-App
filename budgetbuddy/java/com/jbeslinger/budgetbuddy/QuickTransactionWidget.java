package com.jbeslinger.budgetbuddy;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class QuickTransactionWidget extends AppWidgetProvider {

    private static final String SHOW_POPUP_DIALOG_ACTION = "com.jbeslinger.budgetbuddy.showpopupdialog";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, QuickTransactionWidget.class);

        // Obtain all instances of our widget
        // Remember that you can have as many instances of the same widget as you want on the home screen
        int[] allWidgetInstancesIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetInstancesIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_quick_transaction);

            // Create an intent that when received will launch the PopUpActivity
            Intent intent = new Intent(context, QuickTransactionWidget.class);
            intent.setAction(SHOW_POPUP_DIALOG_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Set up the onClickListener of the widget
            // Now, when the widget is pressed the pendingIntent will be sent
            remoteViews.setOnClickPendingIntent(R.id.linearLayout, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        // If the intent is the one that we've defined to launch the pop up dialog
        // then create and launch the PopUpActivity
        if (intent.getAction().equals(SHOW_POPUP_DIALOG_ACTION)) {
            Intent popUpIntent = new Intent(context, QuickTransactionPopUpActivity.class);
            popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(popUpIntent);
        }
        super.onReceive(context, intent);
    }

}
