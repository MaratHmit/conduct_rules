package ru.am.conduct_rules.ui.practice;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;

import ru.am.conduct_rules.DBHelper;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleInfo;
import ru.am.conduct_rules.databinding.FragmentPracticeBinding;

public class PracticeFragment extends Fragment {

    private FragmentPracticeBinding binding;
    private LinearLayout mLinerLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mLinerLayout = root.findViewById(R.id.listPractices);

        loadPractices();

        return root;
    }

    private void loadPractices() {
        Context context = getContext();
        if (context == null)
            return;

        int height = DataModule.convertDpToPixel(130, context);
        int paddingDP = DataModule.convertDpToPixel(4, context);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name" +
                " FROM rule r JOIN practice p WHERE r._id = p.rule_id GROUP BY r._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.name = cursor.getString(1);

                LinearLayout wrapper = new LinearLayout(context);
                wrapper.setTag(rule.id);
                wrapper.setOrientation(LinearLayout.VERTICAL);
                wrapper.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        height));
                wrapper.setBackground(context.getDrawable(R.drawable.cell_shape_orange));

                TextView textViewRule = new TextView(context);
                textViewRule.setText(rule.name);
                textViewRule.setHeight(height);
                textViewRule.setGravity(Gravity.CENTER_HORIZONTAL);
                textViewRule.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textViewRule.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                textViewRule.setPadding(paddingDP, 0, paddingDP, 0);
                wrapper.addView(textViewRule);

                mLinerLayout.addView(wrapper);

            }
        }
        cursor.close();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}