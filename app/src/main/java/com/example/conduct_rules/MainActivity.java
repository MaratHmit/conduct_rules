package com.example.conduct_rules;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.conduct_rules.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static DBHelper mDbHelper;
    private static SQLiteDatabase mDbReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_list_rules, R.id.navigation_practice, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Consts.RESULT_SAVE_USER && resultCode == RESULT_OK)
            updateUserData();
    }

    public static void initDBHelper(Context c) {
        mDbHelper = new DBHelper(c);
        mDbReader = mDbHelper.getReadableDatabase();
    }

    private void updateUserData() {
        if (mDbHelper == null)
            initDBHelper(getBaseContext());

        TextView textViewName = (TextView) findViewById(R.id.textViewUserName);
        TextView textViewGender = (TextView) findViewById(R.id.textViewGender);
        TextView textViewLanguage = (TextView) findViewById(R.id.textViewLanguage);

        Cursor query = mDbReader.rawQuery("SELECT name, gender, language FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            String name = query.getString(0);
            if (textViewName !=  null)
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