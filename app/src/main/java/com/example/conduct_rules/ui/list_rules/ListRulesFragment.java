package com.example.conduct_rules.ui.list_rules;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.widget.Toast;

import com.example.conduct_rules.DBHelper;
import com.example.conduct_rules.R;
import com.example.conduct_rules.RuleInfo;
import com.example.conduct_rules.databinding.FragmentListRulesBinding;

public class ListRulesFragment extends Fragment {

    private ListRulesViewModel homeViewModel;
    private FragmentListRulesBinding binding;
    private static DBHelper mDbHelper;
    private static SQLiteDatabase mDbReader;
    private LinearLayout mLinerLayoutU1;
    private LinearLayout mLinerLayoutU2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(ListRulesViewModel.class);

        binding = FragmentListRulesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mLinerLayoutU1 = root.findViewById(R.id.listRules1);
        mLinerLayoutU2 = root.findViewById(R.id.listRules2);


        initDBHelper(getContext());
        loadListRules();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initDBHelper(Context c) {
        mDbHelper = new DBHelper(c);
        mDbReader = mDbHelper.getReadableDatabase();
    }

    public static int convertPixelsToDp(float px, Context context) {
        return Math.round(px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int convertDpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    View.OnClickListener buttonAddRuleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button buttonAdd = (Button) v;
            RuleInfo rule = (RuleInfo) buttonAdd.getTag();
            String message;
            if (rule.active) {
                message = rule.name + "\nУдалено из практики!";
                buttonAdd.setBackgroundResource(R.drawable.ic_add);
            } else {
                message = rule.name + "\nДобавлено в практику!";
                buttonAdd.setBackgroundResource(R.drawable.ic_remove);
            }
            rule.active = !rule.active;
            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    private void loadListRules() {

        Context context = getContext();
        if (context == null)
            return;

        int height = convertDpToPixel(130, context);
        int sizeButtonAdd = convertDpToPixel(36, context);
        int sizeButtonNot = convertDpToPixel(30, context);
        int paddingDP = convertDpToPixel(4, context);

        Cursor cursor = mDbReader.rawQuery("SELECT _id, code, name, level FROM rule", null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.name = cursor.getString(2);
                rule.level = cursor.getInt(3);
                rule.active = false;

                LinearLayout wrapper = new LinearLayout(context);
                wrapper.setTag(rule.id);
                wrapper.setOrientation(LinearLayout.VERTICAL);
                wrapper.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        height));

                LinearLayout wrapperButton = new LinearLayout(context);
                View emptyView = new View(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
                params.weight = 1;
                emptyView.setLayoutParams(params);
                wrapperButton.addView(emptyView);
                wrapperButton.setOrientation(LinearLayout.HORIZONTAL);
                wrapperButton.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        sizeButtonAdd));

                Button buttonAdd = new Button(context);
                if (rule.level == 1) {
                    buttonAdd.setWidth(sizeButtonAdd);
                    buttonAdd.setHeight(sizeButtonAdd);
                    buttonAdd.setLayoutParams(new FrameLayout.LayoutParams(sizeButtonAdd, sizeButtonAdd));
                    buttonAdd.setBackgroundResource(R.drawable.ic_add);
                    buttonAdd.setOnClickListener(buttonAddRuleClickListener);
                    buttonAdd.setTag(rule);
                } else {
                    buttonAdd.setWidth(sizeButtonNot);
                    buttonAdd.setHeight(sizeButtonNot);
                    buttonAdd.setLayoutParams(new FrameLayout.LayoutParams(sizeButtonNot, sizeButtonNot));
                    buttonAdd.setBackgroundResource(R.drawable.ic_not);
                }

                wrapperButton.addView(buttonAdd);

                wrapper.addView(wrapperButton);

                TextView textViewRule = new TextView(context);
                textViewRule.setText(rule.name);
                textViewRule.setHeight(height);
                textViewRule.setGravity(Gravity.CENTER_HORIZONTAL);
                textViewRule.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textViewRule.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                textViewRule.setPadding(paddingDP, 0, paddingDP, 0);
                wrapper.addView(textViewRule);

                if (rule.level == 1) {
                    wrapper.setBackground(context.getDrawable(R.drawable.cell_shape_orange));
                    mLinerLayoutU1.addView(wrapper);
                } else {
                    wrapper.setBackground(context.getDrawable(R.drawable.cell_shape_gray));
                    mLinerLayoutU2.addView(wrapper);
                }

            }
        }

    }
}