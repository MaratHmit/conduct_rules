package ru.am.conduct_rules;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class RuleCalendar extends LinearLayout {

    public static final String[] DAYS = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private Button[] mButtonDays;

    public RuleCalendar(Context context) {
        super(context);
        inflate(getContext(), R.layout.fragment_rule_calendar, this);

        mButtonDays = new Button[7];
        createButtonsDays();
    }

    private void createButtonsDays() {

        LinearLayout llDaysWeek = findViewById(R.id.ll_days_week);

        int heightRect = DataModule.convertDpToPixel(38, getContext());
        int marginRect = DataModule.convertDpToPixel(3, getContext());
        Typeface font = ResourcesCompat.getFont(getContext(), R.font.manrope_medium);

        for (int i = 0; i < DAYS.length; i++ ) {
            Button buttonDay = new Button(getContext());
            LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(0, heightRect,  1);
            llParam.setMarginStart(marginRect);
            llParam.setMarginEnd(marginRect);
            buttonDay.setLayoutParams(llParam);
            buttonDay.setBackground(getContext().getDrawable(R.drawable.button_day));
            buttonDay.setTypeface(font);
            buttonDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            buttonDay.setText(DAYS[i]);
            buttonDay.setTransformationMethod(null);
            buttonDay.setTextColor(getContext().getColor(R.color.GrayText));

            llDaysWeek.addView(buttonDay);
            mButtonDays[i] = buttonDay;
        }

    }

    public void Update(int ruleID) {

    }

}