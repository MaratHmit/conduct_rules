package ru.am.conduct_rules.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DBHelper;
import ru.am.conduct_rules.ProfileActivity;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private static DBHelper mDbHelper;
    private static SQLiteDatabase mDbReader;

    private TextView mTextViewName;
    private TextView mTextViewGender;
    private TextView mTextViewLanguage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mTextViewName = (TextView) root.findViewById(R.id.textViewUserName);
        mTextViewGender = (TextView) root.findViewById(R.id.textViewGender);
        mTextViewLanguage = (TextView) root.findViewById(R.id.textViewLanguage);

        initDBHelper(getContext());

        ImageButton buttonEdit = (ImageButton) root.findViewById(R.id.buttonEdit);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                FragmentActivity activity = getActivity();
                if (activity != null)
                    activity.startActivityForResult(intent, Consts.RESULT_SAVE_USER);
            }
        });

        Button buttonReset = (Button) root.findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        loadData();


        return root;
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
        Cursor query = mDbReader.rawQuery("SELECT name, gender, language FROM user WHERE _id = 1", null);
        if (query.moveToFirst()) {
            String name = query.getString(0);
            if (mTextViewName != null)
                mTextViewName.setText(name);
            if (mTextViewGender != null) {
                if (!query.isNull(1)) {
                    mTextViewGender.setText("Мужской");
                    if (query.getInt(1) == 1)
                        mTextViewGender.setText("Женский");
                }
            }
            if (mTextViewLanguage != null) {
                mTextViewLanguage.setText("Русский");
                if (query.getInt(1) == 1)
                    mTextViewLanguage.setText("Английский");
            }
        }

    }

}