package ru.am.conduct_rules.ui.list_rules;

import static android.support.v4.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.EstimateActivity;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleDescriptionActivity;
import ru.am.conduct_rules.RuleInfo;
import ru.am.conduct_rules.ui.MainActivity;

public class ViewRuleItem extends LinearLayout {

    private RuleInfo mRule;
    private static int sCurrentDate;
    private boolean isExpandedCalendar;

    public LinearLayout layoutCalendar;

    private LinearLayout mLayoutMain;

    interface OnCheckedRuleListener {
        void updateRules();
    }

    OnCheckedRuleListener onCheckedRuleListener;

    public void setOnCheckedRuleListener(OnCheckedRuleListener ruleListener) {
        this.onCheckedRuleListener = ruleListener;
    }

    private ViewRuleItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private ViewRuleItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private ViewRuleItem(Context context) {
        super(context);
        initView();
    }

    public ViewRuleItem(Context context, RuleInfo rule) {
        super(context);
        setRule(rule);
        initView();
    }

    private void initView() {

        Date currentTime = Calendar.getInstance().getTime();
        sCurrentDate = (int) (currentTime.getTime() / (1000 * 86400));

        inflate(getContext(), R.layout.view_rule_add, this);
        initControls();
    }

    private void initControls() {

        if (mRule == null)
            return;

        TextView tvNumberRule = findViewById(R.id.tv_number_rule);
        tvNumberRule.setText("Пункт № " + mRule.code);
        tvNumberRule.setOnClickListener(view -> showRuleDescription());

        TextView tvShortName = findViewById(R.id.tv_short_name);
        tvShortName.setText(mRule.title);
        tvShortName.setOnClickListener(view -> showRuleDescription());

        TextView tvName = findViewById(R.id.tv_rule_name);
        tvName.setText(mRule.name);

        TextView tvCountDaysP = findViewById(R.id.tv_count_days_p);
        tvCountDaysP.setVisibility(GONE);

        TextView tvCountDaysAll = findViewById(R.id.tv_count_days_all);
        tvCountDaysAll.setVisibility(GONE);

        TextView tvSeparator = findViewById(R.id.tv_separator);
        tvSeparator.setVisibility(GONE);

        ImageView ivLogo = findViewById(R.id.iv_logo_rule);
        ivLogo.setImageResource(R.drawable.logo_red);
        if (mRule.estimate == 2)
            ivLogo.setImageResource(R.drawable.logo_orange);
        if (mRule.estimate == 3)
            ivLogo.setImageResource(R.drawable.logo_green);
        ivLogo.setOnClickListener(view -> showRuleDescription());

        LinearLayout btnAddRule = findViewById(R.id.ll_buttons);
        ImageButton btnDeleteRule = findViewById(R.id.btn_delete_rule);
        if (mRule.mode == 0) {
            btnAddRule.setOnClickListener(buttonAddRuleClickListener);
            btnDeleteRule.setOnClickListener(buttonDeleteRuleClickListener);
        } else {
            btnAddRule.setVisibility(GONE);
        }

        if (!mRule.available)
            setUnavailable();
        else {
            if (mRule.checked)
                setInPractice();
            else
                setOutPractice();
        }

        if (mRule.mode == 1) {
            setSwipePracticeMode();
            updateImageLogoByStatus();
        }
    }

    public void updateImageLogoByStatus() {

        ImageView ivLogo = findViewById(R.id.iv_logo_rule);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, p.result, p.date" +
                        " FROM rule r JOIN practice p ON p.rule_id = r._id WHERE r._id = ?",
                new String[]{String.valueOf((int) mRule.id)});
        if ((cursor != null)) {
            int countSkipped = 0;
            int count = 0;
            while (cursor.moveToNext()) {
                if (cursor.getInt(2) <= sCurrentDate) {
                    count++;
                    if (cursor.getInt(1) == 0)
                        countSkipped++;
                }
            }
            if (count > 3) {
                ivLogo.setImageResource(R.drawable.logo_green);
                if (countSkipped > 1)
                    ivLogo.setImageResource(R.drawable.logo_orange);
                if (countSkipped > 3)
                    ivLogo.setImageResource(R.drawable.logo_red);
            }
        }
    }

    private void showRuleDescription() {
        Intent intent = new Intent(getContext(), RuleDescriptionActivity.class);
        intent.putExtra("title", mRule.title);
        intent.putExtra("description", mRule.description);
        getContext().startActivity(intent);
    }

    private void setSwipePracticeMode() {

        showHideControlsPracticeMode();
        ImageButton btnExpand = findViewById(R.id.btn_expand);
        btnExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isExpandedCalendar = !isExpandedCalendar;
                if (isExpandedCalendar) {
                    btnExpand.setImageResource(R.drawable.collapse_rule);
                    layoutCalendar.setVisibility(VISIBLE);
                    mLayoutMain.setPadding(0, 0, 0, 5);
                }
                else {
                    btnExpand.setImageResource(R.drawable.expand_rule);
                    layoutCalendar.setVisibility(GONE);
                    mLayoutMain.setPadding(0, 0, 0, 50);
                }
            }
        });
        refreshBadge();
    }

    private void showHideControlsPracticeMode() {
        RelativeLayout rlUnavailablePractice = findViewById(R.id.rl_unavailable_practice);
        rlUnavailablePractice.setVisibility(GONE);

        LinearLayout llOutPractice = findViewById(R.id.ll_out_practice);
        llOutPractice.setVisibility(GONE);

        View vRight = findViewById(R.id.v_right);
        vRight.setVisibility(GONE);

        mLayoutMain = findViewById(R.id.ll_rule);
        mLayoutMain.setPadding(0, 0, 0, 50);
        mLayoutMain.setBackgroundResource(R.drawable.frame_corner);
        mLayoutMain.requestLayout();

        RelativeLayout rlInPractice = findViewById(R.id.rl_in_practice);
        rlInPractice.setVisibility(VISIBLE);

        View vNote = findViewById(R.id.tv_note_in_practice);
        vNote.setVisibility(GONE);

        View vClose = findViewById(R.id.btn_delete_rule);
        vClose.setVisibility(GONE);

        TextView tvCountDaysP = findViewById(R.id.tv_count_days_p);
        tvCountDaysP.setVisibility(VISIBLE);

        TextView tvCountDaysAll = findViewById(R.id.tv_count_days_all);
        tvCountDaysAll.setVisibility(VISIBLE);

        TextView tvSeparator = findViewById(R.id.tv_separator);
        tvSeparator.setVisibility(VISIBLE);

        ImageButton btnExpand = findViewById(R.id.btn_expand);
        btnExpand.setVisibility(VISIBLE);

        layoutCalendar = findViewById(R.id.ll_calendar);
        layoutCalendar.setVisibility(GONE);
    }

    private void refreshBadge() {

        TextView tvDaysP = findViewById(R.id.tv_count_days_p);
        TextView tvDaysAll = findViewById(R.id.tv_count_days_all);
        if ((tvDaysP == null) || (tvDaysAll == null))
            return;

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, p.result, p.date" +
                        " FROM rule r JOIN practice p ON p.rule_id = r._id WHERE r._id = ?",
                new String[]{String.valueOf((int) mRule.id)});
        if ((cursor != null)) {
            int count = 0;
            while (cursor.moveToNext()) {
                if (cursor.getInt(2) <= sCurrentDate) {
                    if (cursor.getInt(1) == 1)
                        count++;
                    else
                        count = 0;
                }
            }
            tvDaysP.setText(String.valueOf(count));
        }
    }

    private void setTextColorTextViews(int color) {
        TextView tvNumberRule = findViewById(R.id.tv_number_rule);
        tvNumberRule.setTextColor(color);
        TextView tvShortName = findViewById(R.id.tv_short_name);
        tvShortName.setTextColor(color);
        TextView tvRuleName = findViewById(R.id.tv_rule_name);
        tvRuleName.setTextColor(color);

    }

    private void setRule(RuleInfo rule) {
        mRule = rule;
    }

    public static int getCountPractices() {
        Date currentTime = Calendar.getInstance().getTime();
        int date = (int) (currentTime.getTime() / (1000 * 86400));
        String strDate = String.valueOf(date);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT COUNT(r._id) FROM rule r WHERE checked = 1", null);
        if (cursor.moveToFirst())
            return cursor.getInt(0);
        return 0;
    }

    private boolean isCanRule() {
        int count = getCountPractices();
        if (count >= 5) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Выбор правил для практики")
                    .setMessage("В практику уже добавлено 5 правил!)")
                    .setIcon(R.drawable.ic_do_not_touch)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();

            return false;
        }
        return true;
    }

    View.OnClickListener buttonAddRuleClickListener = v -> {
        if (isCanRule() && updateRule(true))
            setInPractice();
    };

    View.OnClickListener buttonDeleteRuleClickListener = v -> {
        deleteRule();
    };

    private void setUnavailable() {

        RelativeLayout rlInPractice = findViewById(R.id.rl_in_practice);
        rlInPractice.setVisibility(GONE);

        RelativeLayout rlUnavailablePractice = findViewById(R.id.rl_unavailable_practice);
        rlUnavailablePractice.setVisibility(VISIBLE);

        LinearLayout llOutPractice = findViewById(R.id.ll_out_practice);
        llOutPractice.setVisibility(GONE);

        View vRight = findViewById(R.id.v_right);
        vRight.setVisibility(GONE);

        mLayoutMain = findViewById(R.id.ll_rule);
        mLayoutMain.setPadding(0, 0, 0, 60);
        mLayoutMain.requestLayout();

        LinearLayout llButtons = findViewById(R.id.ll_buttons);
        llButtons.setVisibility(GONE);

        setTextColorTextViews(Color.parseColor("#A09FA3"));
    }

    private void setOutPractice() {

        RelativeLayout rlUnavailablePractice = findViewById(R.id.rl_unavailable_practice);
        rlUnavailablePractice.setVisibility(GONE);

        RelativeLayout rlInPractice = findViewById(R.id.rl_in_practice);
        rlInPractice.setVisibility(INVISIBLE);

        LinearLayout llOutPractice = findViewById(R.id.ll_out_practice);
        llOutPractice.setVisibility(VISIBLE);

        View vRight = findViewById(R.id.v_right);
        vRight.setVisibility(VISIBLE);

        mLayoutMain = findViewById(R.id.ll_rule);
        mLayoutMain.setPadding(0, 0, 0, 60);
        mLayoutMain.setBackgroundResource(R.drawable.frame_corner);
        mLayoutMain.requestLayout();

        setTextColorTextViews(Color.BLACK);
    }

    void setInPractice() {

        RelativeLayout rlUnavailablePractice = findViewById(R.id.rl_unavailable_practice);
        rlUnavailablePractice.setVisibility(GONE);

        RelativeLayout rlInPractice = findViewById(R.id.rl_in_practice);
        rlInPractice.setVisibility(VISIBLE);

        View vNote = findViewById(R.id.tv_note_in_practice);
        vNote.setVisibility(VISIBLE);

        View vClose = findViewById(R.id.btn_delete_rule);
        vClose.setVisibility(VISIBLE);

        LinearLayout llOutPractice = findViewById(R.id.ll_out_practice);
        llOutPractice.setVisibility(GONE);

        View vRight = findViewById(R.id.v_right);
        vRight.setVisibility(GONE);

        mLayoutMain = findViewById(R.id.ll_rule);
        mLayoutMain.setPadding(0, 0, 0, 60);
        mLayoutMain.setBackgroundResource(R.drawable.frame_corner);
        mLayoutMain.requestLayout();

        setTextColorTextViews(Color.BLACK);
    }

    private boolean updateRule(boolean checked) {
        try {
            ContentValues cv = new ContentValues();
            mRule.checked = checked;
            cv.put("checked", checked ? 1 : 0);
            DataModule.dbWriter.update("rule", cv, "_id = ?", new String[]{String.valueOf(mRule.id)});
            DataModule.dbWriter.delete("practice", "rule_id = ?", new String[]{String.valueOf(mRule.id)});
            if (mRule.checked) {
                Date currentTime = Calendar.getInstance().getTime();
                int startDate = (int) (currentTime.getTime() / (1000 * 86400));
                int endDate = startDate + 20;
                for (int i = startDate; i <= endDate; i++) {
                    cv.clear();
                    cv.put("rule_id", mRule.id);
                    cv.put("date", i);
                    DataModule.dbWriter.insert("practice", null, cv);
                }
            }
            if (onCheckedRuleListener != null)
                onCheckedRuleListener.updateRules();
            return true;

        } catch (Exception e) {
            Log.e("updateRule", Objects.requireNonNull(e.getMessage()));
        }
        return false;
    }

    private void deleteRule() {
        AlertDialog.Builder ad;
        String title = "Подтверждение удаления";
        String message = "Удалить правило из практики?\n\"" + mRule.name + "\"";
        String buttonYesString = "Да";
        String buttonNoString = "Нет";

        ad = new AlertDialog.Builder(getContext());
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setPositiveButton(buttonYesString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                updateRule(false);
                setOutPractice();
            }
        });
        ad.setNegativeButton(buttonNoString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(false);
        ad.show();
    }




}
