package ru.am.conduct_rules.ui.practice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private Map<Integer, int[]> mapPractice;
    private Map<Integer, Integer> mapCountDays;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mLinerLayoutPractices = root.findViewById(R.id.listPractices);

        listMainRect = new ArrayList<>();
        listFooterRect = new ArrayList<>();
        listPractice = new ArrayList<>();

        mapPractice = new HashMap<Integer, int[]>();
        mapCountDays = new HashMap<Integer, Integer>();

        loadPractices();

        Button buttonMarkCards = (Button) root.findViewById(R.id.button_mark_cards);
        buttonMarkCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), StackActivity.class);
                getContext().startActivity(intent);
            }
        });
        buttonMarkCards.setEnabled(getCountPractices() > 0);


        return root;
    }


    private int getCountPractices() {

        int date = (int) (new Date().getTime() / (1000 * 86400));
        String strDate = String.valueOf(date);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT COUNT(r._id)" +
                " FROM rule r JOIN practice p WHERE r._id = p.rule_id AND p.done = 0 AND p.date = " +
                strDate, null);
        if (cursor.moveToFirst())
            return cursor.getInt(0);
        return 0;
    }


    private void loadPractices() {
        Context context = getContext();
        if (context == null)
            return;

        int index = 0;
        int height = DataModule.convertDpToPixel(90, context);
        int paddingDP = DataModule.convertDpToPixel(4, context);

        initPracticeList();

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id GROUP BY r._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.name = cursor.getString(1);

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
                textViewRule.setBackground(context.getDrawable(R.drawable.cell_shape_orange));
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
        int currDate = (int) (new Date().getTime() / (1000 * 86400));

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT p._id, p.rule_id, p.result, p.done, p.date" +
                " FROM practice p ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                info = new RuleInfo();
                info.id = cursor.getInt(1);
                info.date = cursor.getInt(4);
                int[] days;
                if (mapPractice.get(info.id) == null) {
                    days = new int[21];
                    mapPractice.put(info.id, days);
                    index = 0;
                    mapCountDays.put(info.id, index);
                } else {
                    days = mapPractice.get(info.id);
                    index = mapCountDays.get(info.id);
                }
                if (days != null) {
                    if (info.date <= currDate) {
                        if (cursor.getInt(3) == 0) { // правило пропущено
                            days[index] = 1;
                        } else
                            days[index] = (cursor.getInt(2) == 0) ? 2 : 3;
                        index++;
                        mapCountDays.put(info.id, index);
                    }
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

        int pos = 0;
        int[] days = new int[21];
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
                View rect = new View(context);
                rect.setLayoutParams(new LinearLayout.LayoutParams(0, h, 1));
                rect.setBackground(context.getDrawable(R.drawable.cell_shape_light_gray));
                if ((days != null) && (pos < days.length)) {
                    switch (days[pos]) {
                        case 0:
                            rect.setBackground(context.getDrawable(R.drawable.cell_shape_light_gray));
                            break;
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
                }
                wrapperH.addView(rect);
                pos++;
            }

            layout.addView(wrapperH);
        }

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
        if ((mapCountDays != null) && (mapCountDays.get(ruleID) != null))
            badge.setText(String.valueOf(mapCountDays.get(ruleID)));
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
        int[] days = new int[21];
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
                if ((days != null) && (pos < days.length)) {
                    switch (days[pos]) {
                        case 0:
                            rect.setBackground(context.getDrawable(R.drawable.cell_shape_light_gray));
                            break;
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
                }
                wrapperH.addView(rect);
                pos++;
            }

            wrapperRect.addView(wrapperH);
        }

        layout.addView(wrapperRect);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}