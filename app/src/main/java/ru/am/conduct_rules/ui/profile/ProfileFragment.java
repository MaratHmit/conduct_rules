package ru.am.conduct_rules.ui.profile;

import static ru.am.conduct_rules.Consts.NOTIFY_ID;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DBHelper;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.EstimateActivity;
import ru.am.conduct_rules.Receiver;
import ru.am.conduct_rules.ui.MainActivity;
import ru.am.conduct_rules.ui.ProfileActivity;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private View mRoot;

    private static DBHelper mDbHelper;
    private static SQLiteDatabase mDbReader;

    private LinearLayout mLinerLayoutUserName;
    private LinearLayout mLinerLayoutReminder;
    private TextView mTextViewName;
    private Spinner mSpinnerGender;
    private Spinner mSpinnerLanguage;
    private Spinner mSpinnerMode;
    private Spinner mSpinnerReminder;
    private TextView mTextViewReminderTime;

    private int mHourReminder, mMinReminder;
    private boolean isInit = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mRoot = binding.getRoot();

//        mTextViewName = mRoot.findViewById(R.id.textViewUserName);
//        mTextViewReminderTime = mRoot.findViewById(R.id.textViewReminderTime);
//        mSpinnerGender = mRoot.findViewById(R.id.spinnerGender);
//        mSpinnerLanguage = mRoot.findViewById(R.id.spinnerLanguage);
//        mSpinnerMode = mRoot.findViewById(R.id.spinnerMode);
//        mSpinnerReminder = mRoot.findViewById(R.id.spinnerReminder);
//        mLinerLayoutUserName = mRoot.findViewById(R.id.linerLayoutUserName);
//        mLinerLayoutReminder = mRoot.findViewById(R.id.linerLayoutReminder);
//
//        initDBHelper(getContext());
//
//        loadData();
//        setListeners();

        return mRoot;
    }

    AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            String fieldName = "";
            ContentValues cv = new ContentValues();
            if (parentView == mSpinnerGender)
                fieldName = "gender";
            if (parentView == mSpinnerLanguage)
                fieldName = "language";
            if (parentView == mSpinnerMode)
                fieldName = "mode";
            if (parentView == mSpinnerReminder) {
                fieldName = "reminder";
                mLinerLayoutReminder.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            }
            if (fieldName.isEmpty())
                return;

            if (isInit) {
                cv.put(fieldName, position);
                DataModule.dbWriter.update("user", cv, "_id = 1", null);
            }
            if (parentView == mSpinnerReminder) {
                if (!isInit) {
                    isInit = true;
                    return;
                }
                setNotification();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }

    };

    private void setListeners() {

        mLinerLayoutUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                FragmentActivity activity = getActivity();
                if (activity != null)
                    activity.startActivityForResult(intent, Consts.RESULT_SAVE_USER);
            }
        });

        mSpinnerGender.setOnItemSelectedListener(selectedListener);
        mSpinnerLanguage.setOnItemSelectedListener(selectedListener);
        mSpinnerMode.setOnItemSelectedListener(selectedListener);
        mSpinnerReminder.setOnItemSelectedListener(selectedListener);

        mLinerLayoutReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getActivity(), timeSetListener,
                        mHourReminder, mMinReminder, true)
                        .show();
            }
        });

        mTextViewReminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getActivity(), timeSetListener,
                        mHourReminder, mMinReminder, true)
                        .show();
            }
        });

        Button buttonReset = mRoot.findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
            }
        });


        Button buttonRunEstimate = mRoot.findViewById(R.id.buttonRunEstimate);
        buttonRunEstimate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                runEstimate();
            }
        });

    }

    private void runEstimate() {

        Intent intent = new Intent(getContext(), EstimateActivity.class);
        startActivityForResult(intent, Consts.RESULT_ESTIMATE);
    }

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            mHourReminder = hourOfDay;
            mMinReminder = minute;
            String time = String.format("%02d", mHourReminder) + ":" + String.format("%02d", mMinReminder);
            mTextViewReminderTime.setText(time);
            ContentValues cv = new ContentValues();
            int timeInt = hourOfDay * 60 + minute;
            cv.put("reminder_time", timeInt);
            DataModule.dbWriter.update("user", cv, "_id = 1", null);
            setNotification();

        }
    };

    private void setNotification() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, mHourReminder);
        calendar.set(Calendar.MINUTE, mMinReminder);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        try {
            Intent notifyIntent = new Intent(getActivity(), Receiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast
                    (getContext(), NOTIFY_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Cursor query = mDbReader.rawQuery(
                    "SELECT reminder FROM user WHERE _id = 1", null);
            if (query.moveToFirst()) {
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                if (query.getInt(0) == 1)
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_FIFTEEN_MINUTES / 30, pendingIntent);
            }
        } catch (Exception e) {
            Log.e("PendingIntent", e.getMessage());

        }
    }

    private void resetData() {
        AlertDialog.Builder ad;
        String title = "Подтверждение удаления данных";
        String message = "Очистить все данные?";
        String buttonYesString = "Да";
        String buttonNoString = "Нет";

        ad = new AlertDialog.Builder(getContext());
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setPositiveButton(buttonYesString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                removeData();
                Toast toast = Toast.makeText(getContext(), "Данные очищены!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        ad.setNegativeButton(buttonNoString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(false);
        ad.show();
    }

    private void removeData() {
        DataModule.dbWriter.execSQL("DELETE FROM practice");
        DataModule.dbWriter.execSQL("UPDATE rule SET checked = 0, done = 0, estimate = 0");
        DataModule.dbWriter.execSQL("UPDATE rule SET available = 0 WHERE level = 2");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static void initDBHelper(Context c) {
        mDbHelper = new DBHelper(c);
        mDbReader = mDbHelper.getReadableDatabase();
    }

    private void loadData() {

        Cursor query = mDbReader.rawQuery(
                "SELECT name, gender, language, mode, reminder, reminder_time FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            String name = query.getString(0);
            if (mTextViewName != null)
                mTextViewName.setText(name);
            if (mSpinnerGender != null)
                mSpinnerGender.setSelection(query.getInt(1));
            if (mSpinnerLanguage != null)
                mSpinnerLanguage.setSelection(query.getInt(2));
            if (mSpinnerMode != null)
                mSpinnerMode.setSelection(query.getInt(3));
            if (mSpinnerReminder != null)
                mSpinnerReminder.setSelection(query.getInt(4));
            if (mTextViewReminderTime != null) {
                int t = query.getInt(5);
                mHourReminder = t / 60;
                mMinReminder = t % 60;
                String time = String.format("%02d", mHourReminder) + ":" + String.format("%02d", mMinReminder);
                mTextViewReminderTime.setText(time);
            }


        }

    }

}