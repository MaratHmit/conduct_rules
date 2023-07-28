package ru.am.conduct_rules.ui.practice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleInfo;
import ru.am.conduct_rules.databinding.FragmentPracticeBinding;
import ru.am.conduct_rules.ui.StackActivity;

public class PracticeFragment extends Fragment {

    private FragmentPracticeBinding binding;
    private LinearLayout mLinerLayoutPractices;
    private ArrayList<View> listPractice;
    private ArrayList<View> listMainRect;
    private ArrayList<View> listFooterRect;

    private Map<Integer, RuleInfo[]> mapPractice;
    private Map<Integer, Integer> mapCountDays;

    static public int lastID;
    static public Button buttonMarkCards;
    static public ArrayList<View> listViews = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mLinerLayoutPractices = root.findViewById(R.id.listPractices);

        listMainRect = new ArrayList<>();
        listFooterRect = new ArrayList<>();
        listPractice = new ArrayList<>();

        mapPractice = new HashMap<Integer, RuleInfo[]>();
        mapCountDays = new HashMap<Integer, Integer>();

        lastID = getId();

        loadPractices();

        buttonMarkCards = (Button) root.findViewById(R.id.button_mark_cards);
        buttonMarkCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), StackActivity.class);
                FragmentActivity activity = getActivity();
                if (activity != null)
                    activity.startActivityForResult(intent, Consts.RESULT_FINISH);
            }
        });
        buttonMarkCards.setEnabled(getCountPractices() > 0);


        return root;
    }

    static public int getCountPractices() {

        Date currentTime = Calendar.getInstance().getTime();
        int date = (int) (currentTime.getTime() / (1000 * 86400));
        String strDate = String.valueOf(date);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT COUNT(r._id)" +
                " FROM rule r JOIN practice p WHERE r._id = p.rule_id AND p.done = 0 AND p.date = " +
                strDate, null);
        if (cursor.moveToFirst())
            return cursor.getInt(0);
        return 0;
    }

    static public void updateRectViews() {

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT p._id, p.result, p.done" +
                " FROM practice p ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                for (int i = 0; i < listViews.size(); i++) {
                    View rect = listViews.get(i);
                    if (rect == null)
                        continue;
                    RuleInfo info = (RuleInfo) rect.getTag();
                    if (info == null)
                        continue;

                    if ((info.practiceId == cursor.getInt(0)) && (cursor.getInt(1) > 0)) {
                        info.status = (cursor.getInt(1) == 0) ? 2 : 3;
                        switch (info.status) {
                            case 1:
                                rect.setBackground(rect.getContext().getDrawable(R.drawable.cell_shape_yellow));
                                break;
                            case 2:
                                rect.setBackground(rect.getContext().getDrawable(R.drawable.cell_shape_red));
                                break;
                            case 3:
                                rect.setBackground(rect.getContext().getDrawable(R.drawable.cell_shape_green));
                                break;
                        }
                    }

                }
            }
        }
    }


    private void loadPractices() {
        Context context = getContext();
        if (context == null)
            return;

        int index = 0;
        int height = DataModule.convertDpToPixel(90, context);
        int paddingDP = DataModule.convertDpToPixel(4, context);

        initPracticeList();

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name, r.done" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id GROUP BY r._id ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.name = cursor.getString(1);
                rule.done = cursor.getInt(2);

                LinearLayout wrapperPractice = new LinearLayout(context);
                wrapperPractice.setOrientation(LinearLayout.VERTICAL);
                wrapperPractice.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        height));
                listPractice.add(wrapperPractice);

                LinearLayout wrapperPracticeHeader = new LinearLayout(context);
                wrapperPracticeHeader.setOrientation(LinearLayout.HORIZONTAL);
                wrapperPracticeHeader.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        height));
                wrapperPracticeHeader.setBackground(context.getDrawable(R.drawable.cell_shape));
                wrapperPractice.addView(wrapperPracticeHeader);

                LinearLayout wrapperPracticeFooter = new LinearLayout(context);
                wrapperPracticeFooter.setOrientation(LinearLayout.VERTICAL);
                wrapperPracticeFooter.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        height));
                wrapperPracticeFooter.setBackground(context.getDrawable(R.drawable.cell_shape));
                wrapperPractice.addView(wrapperPracticeFooter);
                wrapperPracticeFooter.setVisibility(View.GONE);

                TextView textViewRule = new TextView(context);
                textViewRule.setText(rule.name);
                textViewRule.setHeight(height);
                textViewRule.setGravity(Gravity.CENTER);
                textViewRule.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textViewRule.setLayoutParams(new LinearLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT, 1));
                textViewRule.setPadding(paddingDP, 0, paddingDP, 0);
                textViewRule.setBackground(context.getDrawable(R.drawable.cell_shape_light_red));
                if (rule.done == 1)
                    textViewRule.setBackground(context.getDrawable(R.drawable.cell_shape_light_orange));
                if (rule.done == 2)
                    textViewRule.setBackground(context.getDrawable(R.drawable.cell_shape_light_green));
                wrapperPracticeHeader.addView(textViewRule);

                setProgressHeader(wrapperPracticeHeader, rule.id, index);
                setProgressFooter(wrapperPracticeFooter, rule.id);
                listFooterRect.add(wrapperPracticeFooter);

                mLinerLayoutPractices.addView(wrapperPractice);
                index++;

            }
        }
        cursor.close();

    }

    private void initPracticeList() {

        RuleInfo info;
        int index;
        Date currentTime = Calendar.getInstance().getTime();
        int currDate = (int) (currentTime.getTime() / (1000 * 86400));

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT p._id, p.rule_id, p.result, p.done, p.date" +
                " FROM practice p ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                info = new RuleInfo();
                info.id = cursor.getInt(1);
                info.date = cursor.getInt(4);
                info.practiceId = cursor.getInt(0);
                RuleInfo[] days;
                if (mapPractice.get(info.id) == null) {
                    days = new RuleInfo[21];
                    mapPractice.put(info.id, days);
                    index = 0;
                    mapCountDays.put(info.id, index);
                } else {
                    days = mapPractice.get(info.id);
                    index = mapCountDays.get(info.id);
                }
                if (days != null) {
                    if (info.date <= currDate) {
                        if (cursor.getInt(3) == 0)
                            info.status = 1;
                        else
                            info.status = (cursor.getInt(2) == 0) ? 2 : 3;
                    }
                    days[index] = info;
                    index++;
                    mapCountDays.put(info.id, index);
                }
            }
        }

    }

    private void setProgressFooter(LinearLayout layout, int ruleID) {

        Context context = getContext();
        if (context == null)
            return;

        int size1 = DataModule.convertDpToPixel(1, context);
        int height = DataModule.convertDpToPixel(90, context);
        Date currentTime = Calendar.getInstance().getTime();
        int currentDate = (int) (currentTime.getTime() / (1000 * 86400));

        int pos = 0;
        RuleInfo[] days = new RuleInfo[21];
        if (mapPractice.get(ruleID) != null)
            days = mapPractice.get(ruleID);
        int h;
        for (int i = 0; i < 3; i++) {
            LinearLayout wrapperH = new LinearLayout(context);
            wrapperH.setOrientation(LinearLayout.HORIZONTAL);
            h = (i == 2) ? ((height / 3) + size1) : (height / 3);
            wrapperH.setLayoutParams(new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, h));
            wrapperH.setWeightSum(7);

            for (int j = 0; j < 7; j++) {
                TextView rect = new TextView(context);
                rect.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                rect.setGravity(Gravity.CENTER);
                rect.setLayoutParams(new LinearLayout.LayoutParams(0, h, 1));
                rect.setBackground(context.getDrawable(R.drawable.cell_shape_light_gray));
                if ((days != null) && (pos < days.length) && (days[pos] != null)) {
                    long dateInt = (long) days[pos].date * 1000 * 86400;
                    SimpleDateFormat fmt = new SimpleDateFormat("E dd.MM");
                    String dateStr = fmt.format(dateInt);
                    rect.setText(dateStr);
                    if (days[pos].date <= currentDate) {
                        switch (days[pos].status) {
                            case 1:
                                rect.setBackground(context.getDrawable(R.drawable.cell_shape_yellow));
                                break;
                            case 2:
                                rect.setBackground(context.getDrawable(R.drawable.cell_shape_red));
                                break;
                            case 3:
                                rect.setBackground(context.getDrawable(R.drawable.cell_shape_green));
                                break;
                        }

                        rect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                handlerRect(v);
                            }
                        });

                        rect.setTag(days[pos]);
                        listViews.add(rect);
                    }
                }

                wrapperH.addView(rect);
                pos++;
            }

            layout.addView(wrapperH);
        }

    }

    private void handlerRect(View v) {

        RuleInfo info = (RuleInfo) v.getTag();
        if (info == null)
            return;

        Intent intent = new Intent(getContext(), StackActivity.class);
        intent.putExtra("practiceID", info.practiceId);
        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.startActivityForResult(intent, Consts.RESULT_FINISH);
    }

    private void setProgressHeader(LinearLayout layout, int ruleID, int index) {

        Context context = getContext();
        if (context == null)
            return;

        int widthL = DataModule.convertDpToPixel(80, context);
        int size = DataModule.convertDpToPixel(30, context);
        int size1 = DataModule.convertDpToPixel(1, context);
        int sizeI = DataModule.convertDpToPixel(26, context);
        int height = DataModule.convertDpToPixel(90, context);
        int widthButtons = DataModule.convertDpToPixel(35, context);
        int marginB = DataModule.convertDpToPixel(2, context);
        int marginI = DataModule.convertDpToPixel(4, context);
        int textSize = DataModule.convertDpToPixel(5, context);

        Date currentTime = Calendar.getInstance().getTime();
        int currentDate = (int) (currentTime.getTime() / (1000 * 86400));

        RelativeLayout wrapperButtonV = new RelativeLayout(context);
        wrapperButtonV.setLayoutParams(new FrameLayout.LayoutParams(widthButtons,
                FrameLayout.LayoutParams.MATCH_PARENT));
        wrapperButtonV.setBackground(context.getDrawable(R.drawable.cell_shape));
        layout.addView(wrapperButtonV);

        TextView badge = new TextView(context);
        badge.setBackground(context.getDrawable(R.drawable.bage));
        RelativeLayout.LayoutParams layoutParamsBadge = new RelativeLayout.LayoutParams(sizeI, sizeI);
        layoutParamsBadge.setMargins(marginI, marginI, marginI, marginI);
        layoutParamsBadge.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        wrapperButtonV.addView(badge, layoutParamsBadge);
        badge.setGravity(Gravity.CENTER);
        if ((mapPractice != null) && (mapPractice.get(ruleID) != null))
            badge.setText(String.valueOf(getCountSuccessDays(mapPractice.get(ruleID))));
        badge.setTextSize(textSize);
        badge.setTextColor(Color.WHITE);

        ImageButton buttonPlusMinus = new ImageButton(context);
        buttonPlusMinus.setImageResource(R.drawable.ic_add_30_white);
        buttonPlusMinus.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        buttonPlusMinus.setBackground(context.getDrawable(R.drawable.rounded_button_plus));
        RelativeLayout.LayoutParams layoutParamsButton = new RelativeLayout.LayoutParams(size, size);
        layoutParamsButton.setMargins(marginB, marginB, marginB, marginB);
        layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        wrapperButtonV.addView(buttonPlusMinus, layoutParamsButton);
        buttonPlusMinus.setTag(index);
        buttonPlusMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) ((ImageButton) v).getTag();
                View practice = listPractice.get(i);
                View footer = listFooterRect.get(i);
                View main = listMainRect.get(i);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) practice.getLayoutParams();

                if (footer.getVisibility() == View.GONE) {
                    footer.setVisibility(View.VISIBLE);
                    main.setVisibility(View.GONE);
                    layoutParams.height *= 2;
                    ((ImageButton) v).setImageResource(R.drawable.ic_remove_30_white);
                } else {
                    footer.setVisibility(View.GONE);
                    main.setVisibility(View.VISIBLE);
                    layoutParams.height /= 2;
                    ((ImageButton) v).setImageResource(R.drawable.ic_add_30_white);
                }
                practice.setLayoutParams(layoutParams);
            }
        });

        LinearLayout wrapperRect = new LinearLayout(context);
        wrapperRect.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsW = new LinearLayout.LayoutParams(widthL, height);
        wrapperRect.setLayoutParams(layoutParamsW);
        listMainRect.add(wrapperRect);

        int h;
        int pos = 0;
        RuleInfo[] days = new RuleInfo[21];
        if (mapPractice.get(ruleID) != null)
            days = mapPractice.get(ruleID);
        for (int i = 0; i < 3; i++) {
            LinearLayout wrapperH = new LinearLayout(context);
            wrapperH.setOrientation(LinearLayout.HORIZONTAL);
            h = (i == 2) ? ((height / 3) + size1) : (height / 3);
            wrapperH.setLayoutParams(new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, h));
            wrapperH.setWeightSum(2);

            for (int j = 0; j < 2; j++) {
                View rect = new View(context);
                rect.setLayoutParams(new LinearLayout.LayoutParams(0, h, 1));
                rect.setBackground(context.getDrawable(R.drawable.cell_shape_light_gray));
                if ((days != null) && (pos < days.length) && (days[pos] != null)) {
                    if (days[pos].date <= currentDate) {
                        switch (days[pos].status) {
                            case 1:
                                rect.setBackground(context.getDrawable(R.drawable.cell_shape_yellow));
                                break;
                            case 2:
                                rect.setBackground(context.getDrawable(R.drawable.cell_shape_red));
                                break;
                            case 3:
                                rect.setBackground(context.getDrawable(R.drawable.cell_shape_green));
                                break;
                        }

                        rect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                handlerRect(v);
                            }
                        });

                        rect.setTag(days[pos]);
                        listViews.add(rect);
                    }
                }
                wrapperH.addView(rect);
                pos++;
            }

            wrapperRect.addView(wrapperH);
        }

        layout.addView(wrapperRect);

    }

    private int getCountSuccessDays(RuleInfo[] days) {
        int result = 0;
        for (int i = 0; i < days.length; i++) {
            if ((days[i] != null) && (days[i].status == 3))
                result++;
        }
        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}