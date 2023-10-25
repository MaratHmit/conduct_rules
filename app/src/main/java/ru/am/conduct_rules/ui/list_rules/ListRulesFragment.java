package ru.am.conduct_rules.ui.list_rules;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DBHelper;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleInfo;
import ru.am.conduct_rules.databinding.FragmentListRulesBinding;
import ru.am.conduct_rules.sticky.StickyScrollView;

import java.util.Calendar;
import java.util.Date;

public class ListRulesFragment extends Fragment {

    private FragmentListRulesBinding binding;

    private LinearLayout mLinerLayoutU1;
    private LinearLayout mLinerLayoutU2;

    private View mRoot;
    private Context mContext;
    LinearLayout mContainerBase;
    LinearLayout mContainerAdvanced;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListRulesBinding.inflate(inflater, container, false);
        mRoot = binding.getRoot();
        mContext = getContext();

        mContainerBase = mRoot.findViewById(R.id.ll_list_rules_base);
        mContainerAdvanced = mRoot.findViewById(R.id.ll_list_rules_advanced);

        initCheckButtons();
        initButtonSelectors();
        updateAvailableListRules();
        loadListRules();

        return mRoot;
    }

    private void initCheckButtons() {

        int count = ViewRuleItem.getCountPractices();

        LinearLayout llButtons = mRoot.findViewById(R.id.ll_check_buttons);
        for (int i = 0; i < llButtons.getChildCount(); i++) {
            CheckBox b = (CheckBox) llButtons.getChildAt(i);
            b.setChecked(i < count);
            if (i < count)
                b.setAlpha(1);
            else
                b.setAlpha((float) 0.6);
        }

        TextView tvInfoCount = mRoot.findViewById(R.id.tv_info_count);

        if (count == llButtons.getChildCount())
            tvInfoCount.setText(R.string.select5rules_finish);
        else
            tvInfoCount.setText(R.string.select5rules);
    }

    private void initButtonSelectors() {
        Button buttonBase = mRoot.findViewById(R.id.button_base);
        Button buttonAdvanced = mRoot.findViewById(R.id.button_advanced);
        if (buttonBase != null && buttonAdvanced != null) {
            buttonBase.setSelected(true);
            buttonBase.setTextColor(Color.WHITE);
            buttonBase.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    buttonBase.setSelected(true);
                    buttonBase.setTextColor(Color.WHITE);
                    buttonAdvanced.setSelected(false);
                    buttonAdvanced.setTextColor(Color.DKGRAY);
                    mContainerAdvanced.setVisibility(View.GONE);
                    mContainerBase.setVisibility(View.VISIBLE);

                    return true;
                }
            });

            buttonAdvanced.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    buttonAdvanced.setSelected(true);
                    buttonAdvanced.setTextColor(Color.WHITE);
                    buttonBase.setSelected(false);
                    buttonBase.setTextColor(Color.DKGRAY);
                    mContainerAdvanced.setVisibility(View.VISIBLE);
                    mContainerBase.setVisibility(View.GONE);
                    return true;
                }
            });

        }
    }

    public void clearListRules() {
        mContainerBase.removeAllViews();
        mContainerAdvanced.removeAllViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateRule(RuleInfo rule) {
        ContentValues cv = new ContentValues();
        cv.put("checked", rule.checked ? 1 : 0);
        DataModule.dbWriter.update("rule", cv, "_id = ?", new String[]{String.valueOf(rule.id)});
        DataModule.dbWriter.delete("practice", "rule_id = ?", new String[]{String.valueOf(rule.id)});
        if (rule.checked) {
            Date currentTime = Calendar.getInstance().getTime();
            int startDate = (int) (currentTime.getTime() / (1000 * 86400));
            int endDate = startDate + 20;
            for (int i = startDate; i <= endDate; i++) {
                cv.clear();
                cv.put("rule_id", rule.id);
                cv.put("date", i);
                DataModule.dbWriter.insert("practice", null, cv);
            }
        }
    }

    private void updateAvailableListRules() {
        Cursor cursor = DataModule.dbReader.rawQuery("SELECT _id, level, point " +
                "FROM rule WHERE level = 2 AND available = 0", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                Cursor cursorL = DataModule.dbReader.rawQuery("SELECT COUNT(_id) " +
                        "FROM rule WHERE done != 2 AND level = 1 AND point = ?", new String[]{String.valueOf(cursor.getInt(2))});
                if ((cursorL != null) && cursorL.moveToFirst() && (cursorL.getInt(0) == 0))
                    DataModule.dbWriter.execSQL("UPDATE rule SET available = 1 WHERE _id = " + cursor.getInt(0));
            }
        }
    }

    private void loadListRules() {

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT _id, code, title, name, checked, " +
                "available, done, estimate, level" +
                " FROM rule WHERE vidible = 1 ORDER BY _id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.code = cursor.getString(1);
                rule.title = cursor.getString(2);
                rule.name = cursor.getString(3);
                rule.checked = cursor.getInt(4) == 1;
                rule.available = cursor.getInt(5) == 1;
                rule.done = cursor.getInt(6);
                rule.estimate = cursor.getInt(7);
                rule.level = cursor.getInt(8);

                addViewRule(rule);

            }
        }
        cursor.close();
    }

    private void addViewRule(RuleInfo rule) {
        ViewRuleItem item = new ViewRuleItem(mContext, rule);
        item.setOnCheckedRuleListener(new ViewRuleItem.OnCheckedRuleListener() {
            @Override
            public void updateRules() {
                initCheckButtons();
            }
        });
        if (rule.level == Consts.LEVEL_BASE)
            mContainerBase.addView(item);
        else
            mContainerAdvanced.addView(item);
    }
}
