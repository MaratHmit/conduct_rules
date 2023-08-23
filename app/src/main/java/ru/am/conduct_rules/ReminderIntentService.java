package ru.am.conduct_rules;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationManagerCompat;

import ru.am.conduct_rules.ui.MainActivity;

public class ReminderIntentService extends IntentService {

    public ReminderIntentService() {

        super("ConductRulesService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        NotificationChannel notificationChannel = new NotificationChannel
                (Consts.NOTIFICATION_CHANNEL_ID,
                        "Service",
                        NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("Description");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);

        Notification.Builder builder = new Notification.Builder(this, Consts.NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle("Оценка 16 пунктов");
        builder.setContentText("Пора оценить правила");
        builder.setSmallIcon(R.drawable.ic_notify);
        Intent notifyIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(Consts.NOTIFY_ID, notificationCompat);
    }
}

