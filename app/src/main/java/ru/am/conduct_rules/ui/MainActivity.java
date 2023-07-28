package ru.am.conduct_rules.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DBHelper;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.databinding.ActivityMainBinding;
import ru.am.conduct_rules.ui.practice.PracticeFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    static private Boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_list_rules, R.id.navigation_practice, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        NavGraph graph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);
        DataModule.initDBHelper(this);

        int countPractices = getCountPractices();
        int countPracticesToday = getCountPracticesToday();
        if (countPractices == 0)
            graph.setStartDestination(R.id.navigation_list_rules);
        if ((countPracticesToday > 0) && !isStart) {
            startSlider();
            isStart = true;
        }
        navController.setGraph(graph);
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
        Intent intent = new Intent(this, StackActivity.class);
        startActivityForResult(intent, Consts.RESULT_FINISH);
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
    }

    private void updatePractices() {

        try {
            PracticeFragment.buttonMarkCards.setEnabled(PracticeFragment.getCountPractices() > 0);
            PracticeFragment.updateRectViews();
            PracticeFragment.updateTextViews();
        } catch (Exception e) {
            finish();
            startActivity(getIntent());
        }

    }

    private void updateUserData() {

        TextView textViewName = (TextView) findViewById(R.id.textViewUserName);
        TextView textViewGender = (TextView) findViewById(R.id.textViewGender);
        TextView textViewLanguage = (TextView) findViewById(R.id.textViewLanguage);

        Cursor query = DataModule.dbReader.rawQuery("SELECT name, gender, language FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            String name = query.getString(0);
            if (textViewName != null)
                textViewName.setText(name);
            if (textViewGender != null) {
                if (!query.isNull(1)) {
                    textViewGender.setText("Мужской");
                    if (query.getInt(1) == 1)
                        textViewGender.setText("Женский");
                }
            }
            if (textViewLanguage != null) {
                textViewLanguage.setText("Русский");
                if (query.getInt(1) == 1)
                    textViewLanguage.setText("Английский");
            }

        }

    }


}