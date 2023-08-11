package ru.am.conduct_rules;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import ru.am.conduct_rules.ui.MainActivity;

public class ReminderIntentService  extends IntentService {

    private static final int NOTIFICATION_ID = 3;

    public ReminderIntentService() {
        super("MyNewIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Notification.Builder builder = new Notification.Builder(this, "PRIMARY_CHANNEL_ID");
        builder.setContentTitle("Оценка 16 пунктов");
        builder.setContentText("Пора оценить правила");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Intent notifyIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, notificationCompat);
    }
}

