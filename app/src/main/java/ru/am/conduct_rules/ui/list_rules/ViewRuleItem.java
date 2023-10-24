package ru.am.conduct_rules.ui.list_rules;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleInfo;

public class ViewRuleItem extends LinearLayout {

    private RuleInfo mRule;

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

        inflate(getContext(), R.layout.view_rule_add, this);
        initControls();
    }

    private void initControls() {

        if (mRule == null)
            return;

        TextView tvNumberRule = findViewById(R.id.tv_number_rule);
        tvNumberRule.setText("Правило № " + mRule.code);

        TextView tvShortName = findViewById(R.id.tv_short_name);
        tvShortName.setText(mRule.title);

        TextView tvName = findViewById(R.id.tv_rule_name);
        tvName.setText(mRule.name);

        Button btnAddRule = findViewById(R.id.btn_add_rule);
        if (btnAddRule != null)
            btnAddRule.setOnClickListener(buttonAddRuleClickListener);

        if (mRule.checked)
            setInPractice();
        if (!mRule.available)
            setUnavailable();
    }

    private void setUnavailable() {
        LinearLayout llRule = findViewById(R.id.ll_rule);
        AlphaAnimation alpha = new AlphaAnimation(0.3F, 0.3F);
        alpha.setDuration(0);
        alpha.setFillAfter(true);
        llRule.startAnimation(alpha);

        LinearLayout llButtons = findViewById(R.id.ll_buttons);
        llButtons.setVisibility(GONE);
    }

    private void setRule(RuleInfo rule) {
        mRule = rule;
    }

    private int getCountPractices() {
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

    void setInPractice() {

        RelativeLayout rlInPractice = findViewById(R.id.rl_in_practice);
        rlInPractice.setVisibility(VISIBLE);

        LinearLayout llOutPractice = findViewById(R.id.ll_out_practice);
        llOutPractice.setVisibility(GONE);

        View vRight = findViewById(R.id.v_right);
        vRight.setVisibility(GONE);

        int height = DataModule.convertDpToPixel(120, getContext());

        ImageView ivLogo = findViewById(R.id.iv_logo_rule);
        ivLogo.setBackgroundResource(R.drawable.logo_in_practice);

        LinearLayout llRule = findViewById(R.id.ll_rule);
        llRule.setPadding(0, 0, 0, 20);
        llRule.getLayoutParams().height = height;
        llRule.requestLayout();
    }

    private boolean updateRule(boolean checked) {
        try {
            ContentValues cv = new ContentValues();
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
            return true;

        } catch (Exception e) {
            Log.e("updateRule", Objects.requireNonNull(e.getMessage()));
        }
        return false;
    }


}
