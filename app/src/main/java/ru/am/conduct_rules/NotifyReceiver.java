package ru.am.conduct_rules;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationManagerCompat;

import ru.am.conduct_rules.ui.MainActivity;

public class NotifyReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        showNotification(context);
    }

    private void showNotification(Context context) {

        NotificationChannel notificationChannel = new NotificationChannel
                (Consts.NOTIFICATION_CHANNEL_ID,
                        "Service",
                        NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("Description");

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);

        Notification.Builder builder = new Notification.Builder(context, Consts.NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle("16 пунктов");
        builder.setContentText("- проверь свои достижения");
        builder.setSmallIcon(R.drawable.ic_launcher);

        Intent notifyIntent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(Consts.NOTIFY_ID, notificationCompat);
    }

}
