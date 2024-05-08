package ru.am.conduct_rules;

import static ru.am.conduct_rules.Consts.NOTIFY_ID;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.navigation.ui.AppBarConfiguration;

import java.util.Calendar;

import ru.am.conduct_rules.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityProfileBinding binding;

    private static DBHelper mDbHelper;
    private static SQLiteDatabase mDbWriter;
    private static SQLiteDatabase mDbReader;
    private LinearLayout mLinerLayoutReminder;
    private Spinner mSpinnerReminder;
    private EditText mEditTextReminderTime;
    private int mHourReminder, mMinReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mLinerLayoutReminder = findViewById(R.id.linerLayoutReminder);
        mSpinnerReminder = findViewById(R.id.spinnerReminder);
        mEditTextReminderTime = findViewById(R.id.editTextReminderTime);

        initAdapters();
        initListeners();
    }

    private void initListeners() {

        if (mEditTextReminderTime != null)
            mEditTextReminderTime.setKeyListener(null);

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

        mEditTextReminderTime.setOnClickListener(v -> new TimePickerDialog(ProfileActivity.this, timeSetListener,
                mHourReminder, mMinReminder, true)
                .show());

    }

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            if (parentView == mSpinnerReminder)
                mLinerLayoutReminder.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            save();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            save();
        }
    };

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            mHourReminder = hourOfDay;
            mMinReminder = minute;
            String time = String.format("%02d", mHourReminder) + ":" + String.format("%02d", mMinReminder);
            mEditTextReminderTime.setText(time);
            ContentValues cv = new ContentValues();
            int timeInt = hourOfDay * 60 + minute;
            cv.put("reminder_time", timeInt);
            DataModule.dbWriter.update("user", cv, "_id = 1", null);
            setNotification();
        }
    };

    private void setNotification() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, 1);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, mHourReminder);
        calendar.set(Calendar.MINUTE, mMinReminder);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        try {
            Intent notifyIntent = new Intent(getApplicationContext(), NotifyReceiver.class);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


            boolean isSet = false;
            Cursor query = mDbReader.rawQuery(
                    "SELECT reminder FROM user WHERE _id = 1", null);
            isSet = query.moveToFirst() && (query.getInt(0) == 1);
            for (int i = 0; i < 60; i++) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast
                        (getApplicationContext(), Consts.NOTIFY_ID + i, notifyIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
                if (isSet)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                calendar.add(Calendar.DATE, i + 1);
            }

        } catch (Exception e) {
            Log.e("pe", e.toString());

        }
    }

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

        Spinner spinnerReminder = findViewById(R.id.spinnerReminder);
        ArrayAdapter adapterReminder = ArrayAdapter.createFromResource(this,
                R.array.reminderList, R.layout.spinner_item);
        adapterReminder.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerReminder.setAdapter(adapterReminder);
        spinnerReminder.setOnItemSelectedListener(onItemSelectedListener);
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
        cv.put("reminder", mSpinnerReminder.getSelectedItemPosition());
        mDbWriter.update("user", cv, "_id = 1", null);
    }

    private void loadData() {

        if (mDbHelper == null)
            initDBHelper(getBaseContext());

        EditText editTextName = (EditText) findViewById(R.id.editTextName);
        Spinner spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        Spinner spinnerLanguage = (Spinner) findViewById(R.id.spinnerLanguage);
        Spinner spinnerMode = (Spinner) findViewById(R.id.spinnerMode);

        Cursor query = mDbReader.rawQuery("SELECT name, gender, language, mode, reminder, " +
                "reminder_time FROM user WHERE _id = 1", null);
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

            if (mSpinnerReminder != null)
                mSpinnerReminder.setSelection(query.getInt(4));
            if (mEditTextReminderTime != null) {
                int t = query.getInt(5);
                mHourReminder = t / 60;
                mMinReminder = t % 60;
                String time = String.format("%02d", mHourReminder) + ":" + String.format("%02d", mMinReminder);
                mEditTextReminderTime.setText(time);
            }
        }
    }
}