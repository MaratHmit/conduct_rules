package ru.am.conduct_rules;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.navigation.ui.AppBarConfiguration;

import ru.am.conduct_rules.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityProfileBinding binding;

    private static DBHelper mDbHelper;
    private static SQLiteDatabase mDbWriter;
    private static SQLiteDatabase mDbReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initAdapters();
        initListeners();
    }

    private void initListeners() {

        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        EditText editTextName = (EditText) findViewById(R.id.editTextName);
        editTextName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                save();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        Button buttonReset = findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(v -> resetData());

        Button buttonRunEstimate = findViewById(R.id.buttonRunEstimate);
        buttonRunEstimate.setOnClickListener(v -> runEstimate());
    }

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            save();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            save();
        }
    };

    private void initAdapters() {

        Spinner spinnerGender = findViewById(R.id.spinnerGender);
        ArrayAdapter adapterGender = ArrayAdapter.createFromResource(this,
                R.array.genderList, R.layout.spinner_item);
        adapterGender.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerGender.setAdapter(adapterGender);
        spinnerGender.setOnItemSelectedListener(onItemSelectedListener);

        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        ArrayAdapter adapterLanguage = ArrayAdapter.createFromResource(this,
                R.array.languageList, R.layout.spinner_item);
        adapterLanguage.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapterLanguage);
        spinnerLanguage.setOnItemSelectedListener(onItemSelectedListener);

        Spinner spinnerMode = findViewById(R.id.spinnerMode);
        ArrayAdapter adapterMode = ArrayAdapter.createFromResource(this,
                R.array.modeList, R.layout.spinner_item);
        adapterMode.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMode.setAdapter(adapterMode);
        spinnerMode.setOnItemSelectedListener(onItemSelectedListener);
    }

    @Override
    protected void onStart() {

        super.onStart();
        loadData();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void initDBHelper(Context c) {
        mDbHelper = new DBHelper(c);
        mDbWriter = mDbHelper.getWritableDatabase();
        mDbReader = mDbHelper.getReadableDatabase();
    }

    private void save() {

        Context context = this.getBaseContext();
        if (mDbHelper == null) {
            initDBHelper(context);
        }

        EditText editTextName = (EditText) findViewById(R.id.editTextName);
        Spinner spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        Spinner spinnerLanguage = (Spinner) findViewById(R.id.spinnerLanguage);
        Spinner spinnerMode = (Spinner) findViewById(R.id.spinnerMode);

        if ((editTextName == null) || (spinnerGender == null) || (spinnerLanguage == null))
            return;

        ContentValues cv = new ContentValues();
        cv.put("name", editTextName.getText().toString());
        cv.put("gender", spinnerGender.getSelectedItemPosition());
        cv.put("language", spinnerLanguage.getSelectedItemPosition());
        cv.put("mode", spinnerMode.getSelectedItemPosition());
        mDbWriter.update("user", cv, "_id = 1", null);
    }

    private void loadData() {

        if (mDbHelper == null)
            initDBHelper(getBaseContext());

        EditText editTextName = (EditText) findViewById(R.id.editTextName);
        Spinner spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        Spinner spinnerLanguage = (Spinner) findViewById(R.id.spinnerLanguage);
        Spinner spinnerMode = (Spinner) findViewById(R.id.spinnerMode);

        Cursor query = mDbReader.rawQuery("SELECT name, gender, language, mode FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            String name = query.getString(0);
            if (editTextName != null) {
                editTextName.setText(name);
                editTextName.setSelection(editTextName.length());
            }
            if (spinnerGender != null)
                spinnerGender.setSelection(query.getInt(1));
            if (spinnerLanguage != null)
                spinnerLanguage.setSelection(query.getInt(2));
            if (spinnerMode != null)
                spinnerMode.setSelection(query.getInt(3));
        }
    }

    private void resetData() {
        AlertDialog.Builder ad;
        String title = "Подтверждение удаления данных";
        String message = "Очистить все данные?";
        String buttonYesString = "Да";
        String buttonNoString = "Нет";

        ad = new AlertDialog.Builder(this);
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setPositiveButton(buttonYesString, (dialog, arg1) -> {
            removeData();
            Toast toast = Toast.makeText(this, "Данные очищены!", Toast.LENGTH_SHORT);
            toast.show();
        });
        ad.setNegativeButton(buttonNoString, (dialog, arg1) -> {
        });
        ad.setCancelable(false);
        ad.show();
    }

    private void removeData() {
        if (mDbHelper == null)
            initDBHelper(getBaseContext());

        DataModule.dbWriter.execSQL("DELETE FROM practice");
        DataModule.dbWriter.execSQL("UPDATE rule SET checked = 0, done = 0, estimate = 0");
        DataModule.dbWriter.execSQL("UPDATE rule SET available = 0 WHERE level = 2");
    }

    private void runEstimate() {

        Intent intent = new Intent(getBaseContext(), EstimateActivity.class);
        startActivityForResult(intent, Consts.RESULT_ESTIMATE);
    }

    public void onButtonFeedback(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://t.me/+5x-rmfQUnl1hZDli")));
    }



}