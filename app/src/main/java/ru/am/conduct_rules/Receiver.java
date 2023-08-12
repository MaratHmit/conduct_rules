package ru.am.conduct_rules;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

import java.util.List;

public class Receiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        intent = new Intent(context, ReminderIntentService.class);
        if (isAppForeground(context))
            context.startService(intent);
        else
            context.startForegroundService(intent);

    }

    public boolean isAppForeground(Context mContext) {

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false;
            }
        }

        return true;
    }

}
