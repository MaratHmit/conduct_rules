package ru.am.conduct_rules;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import ru.am.conduct_rules.ui.MainActivity;

public class StartActivity extends AppCompatActivity {

    public static int SPLASH_TIMER = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        DataModule.isFirstStart = isFirstStart();
        DataModule.isSetReminder = !DataModule.isFirstStart;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (DataModule.isFirstStart)
                    intent = new Intent(StartActivity.this, StartLoginActivity.class);
                else
                    intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIMER);
    }

    private boolean isFirstStart() {

        if (DataModule.dbReader == null)
            DataModule.initDBHelper(getBaseContext());

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT start FROM user WHERE _id = 1", null);

        return cursor.moveToFirst() && cursor.getInt(0) == 0;
    }

}
