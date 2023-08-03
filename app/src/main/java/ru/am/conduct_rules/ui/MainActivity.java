package ru.am.conduct_rules.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.databinding.ActivityMainBinding;
import ru.am.conduct_rules.ui.practice.PracticeFragment;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

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
        if ((countPracticesToday > 0))
            startSlider();
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
    }

    private void updatePractices() {

        try {
            PracticeFragment.sButtonMarkCards.setEnabled(PracticeFragment.getCountPractices() > 0);
            PracticeFragment.updateRectViews();
            PracticeFragment.updateTextViews();
            PracticeFragment.updateStatuses(this);
        } catch (Exception e) {
            finish();
            startActivity(getIntent());
        }

    }

    private void updateUserData() {

        TextView textViewName = findViewById(R.id.textViewUserName);
        TextView textViewGender = findViewById(R.id.textViewGender);
        TextView textViewLanguage = findViewById(R.id.textViewLanguage);
        TextView textViewMode = findViewById(R.id.textViewMode);

        Cursor query = DataModule.dbReader.rawQuery("SELECT name, gender, language, mode FROM user WHERE _id = 1", null);
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
                if (query.getInt(2) == 1)
                    textViewLanguage.setText("Английский");
            }
            if (textViewMode != null) {
                textViewMode.setText("Свайп");
                if (query.getInt(3) == 1)
                    textViewMode.setText("Таблица");
            }

        }

    }


}