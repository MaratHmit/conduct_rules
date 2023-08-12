package ru.am.conduct_rules.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EditText textName = (EditText) findViewById(R.id.editTextName);
        textName.requestFocus();

        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        if (buttonBack != null) {
            buttonBack.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        }

        Button buttonSave = (Button) findViewById(R.id.buttonSave);
        if (buttonSave != null) {
            buttonSave.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    save();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }


    private void save() {

        EditText editTextName = findViewById(R.id.editTextName);
        if (editTextName == null)
            return;

        ContentValues cv = new ContentValues();
        cv.put("name", editTextName.getText().toString());
        DataModule.dbWriter.update("user", cv, "_id = 1", null);

        setResult(RESULT_OK);
        finish();
    }

    private void loadData() {

        EditText editTextName = findViewById(R.id.editTextName);

        Cursor query = DataModule.dbReader.rawQuery("SELECT name FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            String name = query.getString(0);
            if (editTextName != null) {
                editTextName.setText(name);
                editTextName.requestFocus();
                editTextName.setSelection(editTextName.length());
            }
        }

    }

}