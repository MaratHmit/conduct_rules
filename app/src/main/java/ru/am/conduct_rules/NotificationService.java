package ru.am.conduct_rules;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import ru.am.conduct_rules.ui.MainActivity;

public class NotificationService extends Service {


    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        showNotification(getBaseContext());

        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void someTask() {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                while (true)
                    requestHttp();
            }
        });
        thread.start();
    }

    private void requestHttp() {

        try {
            URL url = new URL("https://dverra.ru/droid.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if ((connection != null) && (connection.getResponseCode() == HttpURLConnection.HTTP_OK)) {
                // успех
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
