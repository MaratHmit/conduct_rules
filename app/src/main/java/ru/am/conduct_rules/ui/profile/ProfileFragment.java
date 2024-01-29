package ru.am.conduct_rules.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DBHelper;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.EstimateActivity;
import ru.am.conduct_rules.ProfileActivity;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.databinding.FragmentProfileBinding;
import ru.am.conduct_rules.ui.MainActivity;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private View mRoot;
    private static DBHelper mDbHelper;
    private static SQLiteDatabase mDbReader;
    private TextView mTextViewName;
    private TextView mTextViewCountDays;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mRoot = binding.getRoot();

        mTextViewName = mRoot.findViewById(R.id.tv_userName);
        mTextViewCountDays = mRoot.findViewById(R.id.tv_count_days_practice);
        initDBHelper(getContext());

        loadData();
        setListeners();

        return mRoot;
    }

    private void setListeners() {

        ImageButton buttonSettings = mRoot.findViewById(R.id.btn_settings);
        buttonSettings.setOnClickListener(v -> showSettings());

        Button buttonReset = mRoot.findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(v -> resetData());

        Button buttonRunEstimate = mRoot.findViewById(R.id.buttonRunEstimate);
        buttonRunEstimate.setOnClickListener(v -> runEstimate());
    }

    private void showSettings() {

        Intent intent = new Intent(getContext(), ProfileActivity.class);
        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.startActivityForResult(intent, Consts.RESULT_SAVE_USER);
    }

    public static void initDBHelper(Context c) {
        mDbHelper = new DBHelper(c);
        mDbReader = mDbHelper.getReadableDatabase();
    }

    private void loadData() {

        Date currentTime = Calendar.getInstance().getTime();
        int currentDate = (int) (currentTime.getTime() / (1000 * 86400));

        Cursor cursor = mDbReader.rawQuery(
                "SELECT name, gender, language, mode, reminder, reminder_time, reg_date FROM user WHERE _id = 1", null);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            if (mTextViewName != null)
                mTextViewName.setText(name);
            if (mTextViewCountDays != null) {
                int n = (currentDate - cursor.getInt(6)) + 1;
                String s = String.valueOf(n);
                String numS = s.substring(s.length() - 1);
                int num = Integer.parseInt(numS);
                if ((num == 0) || ((num > 4) && (num < 10)))
                    s = s + " дней";
                else if (num == 1)
                    s = s + " день";
                else
                    s = s + " дня";
                s = s + " в практике";
                mTextViewCountDays.setText(s);
            }
        }

    }

    private void resetData() {
        AlertDialog.Builder ad;
        String title = "Подтверждение удаления данных";
        String message = "Очистить все данные?";
        String buttonYesString = "Да";
        String buttonNoString = "Нет";

        ad = new AlertDialog.Builder(getActivity());
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setPositiveButton(buttonYesString, (dialog, arg1) -> {
            removeData();
            Toast toast = Toast.makeText(getActivity(), "Данные очищены!", Toast.LENGTH_SHORT);
            toast.show();
        });
        ad.setNegativeButton(buttonNoString, (dialog, arg1) -> {
        });
        ad.setCancelable(false);
        ad.show();
    }

    private void removeData() {
        if (mDbHelper == null)
            initDBHelper(getActivity());

        DataModule.dbWriter.execSQL("DELETE FROM practice");
        DataModule.dbWriter.execSQL("UPDATE rule SET checked = 0, done = 0, estimate = 0");
        DataModule.dbWriter.execSQL("UPDATE rule SET available = 0 WHERE level = 2");
    }

    private void runEstimate() {

        Intent intent = new Intent(getActivity(), EstimateActivity.class);
        startActivityForResult(intent, Consts.RESULT_ESTIMATE);
    }

}