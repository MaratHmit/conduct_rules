package ru.am.conduct_rules;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

        for (int i = 0; i < DAYS.length; i++) {
            Button buttonDay = new Button(getContext());
            LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(0, heightRect, 1);
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

    public void update(RuleInfo rule) {

        Date currentTime = Calendar.getInstance().getTime();
        int currentDate = (int) (currentTime.getTime() / (1000 * 86400));

        int targetDate = rule.date;
        long targetTime = (long) targetDate * 1000 * 86400;
        SimpleDateFormat fmt = new SimpleDateFormat("E dd.MM");
        String dateTarget = fmt.format(targetTime);
        long dateInt = (long) currentDate * 1000 * 86400;
        String dateCurr = fmt.format(dateInt);

        SimpleDateFormat sdf = new SimpleDateFormat("u");
        int dayOfTheWeek = Integer.parseInt(sdf.format(targetTime)) - 1;

        if (DataModule.dbReader == null)
            DataModule.initDBHelper(getContext());

        int ruleID = rule.id;
        int startDate = targetDate - dayOfTheWeek;
        int endDate = startDate + 6;

        dateInt = (long) startDate * 1000 * 86400;
        String dateStart = fmt.format(dateInt);
        dateInt = (long) endDate * 1000 * 86400;
        String dateEnd = fmt.format(dateInt);

        Typeface font = ResourcesCompat.getFont(getContext(), R.font.manrope_medium);
        for (int i = 0; i < DAYS.length; i++) {
            mButtonDays[i].setTextColor(getContext().getColor(R.color.GrayText));
            mButtonDays[i].setTypeface(font);
        }
        font = ResourcesCompat.getFont(getContext(), R.font.manrope_extrabold);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT p._id, p.result, p.done, p.date" +
                        " FROM practice p WHERE p.rule_id = ? AND p.date >= ? AND p.date <= ?",
                new String[]{String.valueOf(ruleID), String.valueOf(startDate), String.valueOf(endDate)});
        int index = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                index = cursor.getInt(3) - startDate;
                mButtonDays[index].setTextColor(getContext().getColor(R.color.Red));
                if (cursor.getInt(1) == 1)
                    mButtonDays[index].setTextColor(getContext().getColor(R.color.Green));
                if (index == dayOfTheWeek)
                    mButtonDays[index].setTypeface(font);
                if (cursor.getInt(3) == currentDate)
                    break;
            }
        }
    }

}