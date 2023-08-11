package ru.am.conduct_rules;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    public void onReceive (Context context , Intent intent) {

        Intent intentReminder = new Intent(context, ReminderIntentService.class);
        context.startService(intentReminder);

    }

}
