package ru.am.conduct_rules.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DBHelper;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mRoot = binding.getRoot();

        mTextViewName = mRoot.findViewById(R.id.tv_userName);
        initDBHelper(getContext());

        loadData();
        setListeners();

        return mRoot;
    }

    private void setListeners() {

        ImageButton buttonSettings = mRoot.findViewById(R.id.btn_settings);
        buttonSettings.setOnClickListener(v -> showSettings());
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

        Cursor query = mDbReader.rawQuery(
                "SELECT name, gender, language, mode, reminder, reminder_time FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            String name = query.getString(0);
            if (mTextViewName != null)
                mTextViewName.setText(name);
        }

    }

}