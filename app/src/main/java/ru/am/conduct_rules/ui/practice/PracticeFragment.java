package ru.am.conduct_rules.ui.practice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import ru.am.conduct_rules.Consts;
import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.ProfileActivity;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleInfo;
import ru.am.conduct_rules.databinding.FragmentPracticeBinding;

public class PracticeFragment extends Fragment {

    private FragmentPracticeBinding binding;
    private LinearLayout mLinerLayoutPractices;
    private ArrayList<View> listPractice;
    private ArrayList<View> listMainRect;
    private ArrayList<View> listFooterRect;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mLinerLayoutPractices = root.findViewById(R.id.listPractices);

        listMainRect = new ArrayList();
        listFooterRect = new ArrayList();
        listPractice = new ArrayList();

        loadPractices();

        return root;
    }

    private void loadPractices() {
        Context context = getContext();
        if (context == null)
            return;

        int index = 0;
        int height = DataModule.convertDpToPixel(90, context);
        int paddingDP = DataModule.convertDpToPixel(4, context);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name" +
                " FROM rule r JOIN practice p WHERE r._id = p.rule_id GROUP BY r._id", null);
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

    private void setProgressFooter(LinearLayout layout, int id) {

        Context context = getContext();
        if (context == null)
            return;

        int size1 = DataModule.convertDpToPixel(1, context);
        int height = DataModule.convertDpToPixel(90, context);

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
                wrapperH.addView(rect);
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
        badge.setText("3");
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
                } else {
                    footer.setVisibility(View.GONE);
                    main.setVisibility(View.VISIBLE);
                    layoutParams.height /= 2;
                }
                practice.setLayoutParams(layoutParams);
            }
        });

        LinearLayout wrapperRect = new LinearLayout(context);
        wrapperRect.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsW = new LinearLayout.LayoutParams( widthL, height);
        wrapperRect.setLayoutParams(layoutParamsW);
        listMainRect.add(wrapperRect);

        int h;
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
                wrapperH.addView(rect);
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