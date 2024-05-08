package ru.am.conduct_rules.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.Calendar;
import java.util.Date;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.NotifyReceiver;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.databinding.ActivityMainBinding;
import ru.am.conduct_rules.ui.practice.PracticeFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Context context;

    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = binding.getRoot().getContext();

        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
        NavGraph graph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);

        DataModule.initDBHelper(this);


        int countPractices = getCountPractices();
        int countPracticesToday = getCountPracticesToday();
        if (countPractices == 0)
            graph.setStartDestination(R.id.navigation_list_rules);
        if ((countPracticesToday > 0))
            startSlider();
        else
            checkSkippedPractices();
        navController.setGraph(graph);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();

        if (!DataModule.isSetReminder)
            setNotification();
    }


    private void checkSkippedPractices() {
        PracticeFragment.updateStatuses(this);
    }

    private int getCountPracticesToday() {

        Date currentTime = Calendar.getInstance().getTime();
        int date = (int) (currentTime.getTime() / (1000 * 86400));
        String strDate = String.valueOf(date);

        Cursor cursor = DataModule.dbReader.rawQuery(
                "SELECT COUNT(p._id)" +
                        " FROM rule r JOIN practice p ON r._id = p.rule_id " +
                        " WHERE p.done = 0 AND p.date = " + strDate +
                        " GROUP BY r._id", null);
        if (cursor.moveToFirst())
            return cursor.getInt(0);
        return 0;
    }

    private void startSlider() {

        Cursor query = DataModule.dbReader.rawQuery("SELECT mode FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            if (query.getInt(0) == 0) {
                Intent intent = new Intent(this, StackActivity.class);
                startActivityForResult(intent, Consts.RESULT_FINISH);
            }
        }

    }

    private int getCountPractices() {
        Cursor cursor = DataModule.dbReader.rawQuery("SELECT COUNT(r._id)" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id", null);
        if (cursor.moveToFirst())
            return cursor.getInt(0);
        return 0;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Consts.RESULT_SAVE_USER && resultCode == RESULT_OK)
            updateUserData();

        if (requestCode == Consts.RESULT_FINISH && resultCode == RESULT_OK)
            updatePractices();

        if (requestCode == Consts.RESULT_ESTIMATE) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    private void updatePractices() {

        try {
            PracticeFragment.sButtonMarkCards.setEnabled(PracticeFragment.getCountPractices() > 0);
            PracticeFragment.updateRectViews();
            PracticeFragment.updateTextViews();
            PracticeFragment.updateRuleItems(this);
            PracticeFragment.updateStatuses(this);
        } catch (Exception e) {
            finish();
            startActivity(getIntent());
        }

    }

    private void updateUserData() {

        TextView textViewName = findViewById(R.id.tv_userName);

        Cursor query = DataModule.dbReader.rawQuery("SELECT name FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            String name = query.getString(0);
            if (textViewName != null)
                textViewName.setText(name);

        }

    }

    public void onButtonFeedback(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://t.me/+5x-rmfQUnl1hZDli")));
    }

    private void setNotification() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, 1);

        int hour = 20;
        int min = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        try {

            Intent notifyIntent = new Intent(getApplicationContext(), NotifyReceiver.class);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            for (int i = 0; i < 60; i++) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast
                        (getApplicationContext(), Consts.NOTIFY_ID + i, notifyIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                calendar.add(Calendar.DATE, i + 1);
            }


        } catch (Exception e) {

        }
    }

}