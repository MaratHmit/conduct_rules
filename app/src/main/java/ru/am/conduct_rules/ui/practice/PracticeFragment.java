package ru.am.conduct_rules.ui.practice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    private FragmentPracticeBinding mBinding;
    private LinearLayout mLinerLayoutPractices;
    private LinearLayout mLinerLayoutTable;
    private ScrollView mScrollViewPractice;
    private ArrayList<View> mListMainRect;
    private ArrayList<View> mListFooterRect;

    private Map<Integer, RuleInfo[]> mMapPractice;
    private Map<Integer, Integer> mMapCountDays;
    private static int sCurrentDate;
    private Button mButtonMode;
    private int mMode;
    private ArrayList<TextView> listBadgesTable;

    static public Button sButtonMarkCards;
    static public ArrayList<View> sListPractice;
    static public ArrayList<TextView> sListRect;
    static public ArrayList<TextView> sListTextViews;
    static public ArrayList<TextView> sListBadges;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentPracticeBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        mLinerLayoutPractices = root.findViewById(R.id.listPracticesSwipe);
        mLinerLayoutTable = root.findViewById(R.id.listPracticesTable);
        mScrollViewPractice = root.findViewById(R.id.scrollViewPractice);

        mListMainRect = new ArrayList<>();
        mListFooterRect = new ArrayList<>();

        sListPractice = new ArrayList<>();
        sListRect = new ArrayList<>();
        sListTextViews = new ArrayList<>();
        sListBadges = new ArrayList<>();

        listBadgesTable = new ArrayList<>();

        Date currentTime = Calendar.getInstance().getTime();
        sCurrentDate = (int) (currentTime.getTime() / (1000 * 86400));

        mMapPractice = new HashMap<Integer, RuleInfo[]>();
        mMapCountDays = new HashMap<Integer, Integer>();

        mMode = readMode();

        mButtonMode = root.findViewById(R.id.button_mode);
        mButtonMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMode();
            }
        });

        sButtonMarkCards = root.findViewById(R.id.button_mark_cards);
        sButtonMarkCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), StackActivity.class);
                FragmentActivity activity = getActivity();
                if (activity != null)
                    activity.startActivityForResult(intent, Consts.RESULT_FINISH);
            }
        });
        sButtonMarkCards.setEnabled(getCountPractices() > 0);

        initPractices();

        return root;
    }

    private int readMode() {
        Cursor query = DataModule.dbReader.rawQuery("SELECT mode FROM user WHERE _id = 1", null);
        if (query.moveToFirst())
            return query.getInt(0);
        return 0;
    }

    private void changeMode() {

        mMode = (mMode == 0) ? 1 : 0;

        ContentValues cv = new ContentValues();
        cv.put("mode", mMode);
        DataModule.dbWriter.update("user", cv, "_id = 1", null);

        mLinerLayoutTable.removeAllViews();
        mLinerLayoutPractices.removeAllViews();

        clearLists();
        initPractices();
    }

    private void clearLists() {

        mListMainRect.clear();
        mListFooterRect.clear();
        sListPractice.clear();
        sListRect.clear();
        sListTextViews.clear();
        sListBadges.clear();
        listBadgesTable.clear();

        mMapPractice.clear();
        mMapCountDays.clear();
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

    static public void updateTextViews() {
        Cursor cursor = DataModule.dbReader.rawQuery("SELECT _id, done FROM rule", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                for (int i = 0; i < sListTextViews.size(); i++) {
                    TextView view = sListTextViews.get(i);
                    if ((int) view.getTag() == cursor.getInt(0)) {
                        if (cursor.getInt(1) == 1)
                            view.setBackground(view.getContext().getDrawable(R.drawable.cell_shape_light_orange));
                        if (cursor.getInt(1) == 2)
                            view.setBackground(view.getContext().getDrawable(R.drawable.cell_shape_light_green));
                    }
                }
            }
        }

    }

    static public void updateRectViews() {

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT p._id, p.result, p.done, p.date" +
                " FROM practice p ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                for (int i = 0; i < sListRect.size(); i++) {
                    TextView rect = sListRect.get(i);
                    if (rect == null)
                        continue;
                    RuleInfo info = (RuleInfo) rect.getTag();
                    if (info == null)
                        continue;

                    if ((info.practiceId == cursor.getInt(0))) {
                        info.status = (cursor.getInt(1) == 0) ? 2 : 3;
                        info.date = cursor.getInt(3);
                        info.done = cursor.getInt(2);
                        rect.setBackground(rect.getContext().getDrawable(R.drawable.cell_shape));
                        if (info.date <= sCurrentDate)
                            rect.setBackground(rect.getContext().getDrawable(R.drawable.cell_shape_yellow));
                        if (info.done == 1) {
                            switch (info.status) {
                                case 2:
                                    rect.setBackground(rect.getContext().getDrawable(R.drawable.cell_shape_red));
                                    break;
                                case 3:
                                    rect.setBackground(rect.getContext().getDrawable(R.drawable.cell_shape_green));
                                    break;
                            }
                        }
                        long dateInt = (long) info.date * 1000 * 86400;
                        String s = "E dd.MM";
                        if (rect.getText().length() < 4)
                            s = "E";
                        SimpleDateFormat fmt = new SimpleDateFormat(s);
                        String dateStr = fmt.format(dateInt);
                        rect.setText(dateStr);
                        updateBadges();
                    }

                }
            }
        }
    }

    private static void updateBadges() {
        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, COUNT(p._id)" +
                        " FROM rule r JOIN practice p ON p.rule_id = r._id WHERE p.result = 1 GROUP BY r._id",
                null);
        if ((cursor != null)) {
            try {
                while (cursor.moveToNext()) {
                    for (int i = 0; i < sListBadges.size(); i++)
                        if ((int) sListBadges.get(i).getTag() == cursor.getInt(0)) {
                            int count = cursor.getInt(1);
                            sListBadges.get(i).setText(String.valueOf(count));
                        }
                }
            } catch (Exception e) {

            }
        }
    }


    private void initPractices() {

        if (mMode == 0) {
            mLinerLayoutTable.setVisibility(View.GONE);
            mScrollViewPractice.setVisibility(View.VISIBLE);
            sButtonMarkCards.setVisibility(View.VISIBLE);
            mButtonMode.setText("Таблица");
            createPracticesSwipe();
            return;
        }

        mLinerLayoutTable.setVisibility(View.VISIBLE);
        mScrollViewPractice.setVisibility(View.GONE);
        sButtonMarkCards.setVisibility(View.GONE);
        mButtonMode.setText("Свайп режим");
        createPracticesTable();
    }

    private void createPracticesSwipe() {

        Context context = getContext();
        if (context == null)
            return;

        int index = 0;
        int height = DataModule.convertDpToPixel(90, context);
        int widthNum = DataModule.convertDpToPixel(30, context);
        int paddingDP = DataModule.convertDpToPixel(4, context);

        initPracticeList();

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name, r.done, r.code" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id GROUP BY r._id ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.name = cursor.getString(1);
                rule.done = cursor.getInt(2);
                rule.code = cursor.getString(3);

                LinearLayout wrapperPractice = new LinearLayout(context);
                wrapperPractice.setTag(rule.id);
                wrapperPractice.setOrientation(LinearLayout.VERTICAL);
                wrapperPractice.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        height));
                sListPractice.add(wrapperPractice);

                LinearLayout wrapperPracticeHeader = new LinearLayout(context);

                rule.code = rule.code.replace(".", "\n-\n");
                TextView textViewNum = new TextView(context);
                textViewNum.setText(rule.code);
                textViewNum.setGravity(Gravity.CENTER);
                textViewNum.setPadding(paddingDP, 0, paddingDP, 0);
                textViewNum.setLayoutParams(new FrameLayout.LayoutParams(widthNum, FrameLayout.LayoutParams.MATCH_PARENT));
                textViewNum.setBackground(context.getDrawable(R.drawable.cell_shape_dark));
                textViewNum.setTextColor(Color.WHITE);
                textViewNum.setTypeface(null, Typeface.BOLD);
                textViewNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                wrapperPracticeHeader.addView(textViewNum);

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
                textViewRule.setTag(rule.id);
                sListTextViews.add(textViewRule);

                setProgressHeader(wrapperPracticeHeader, rule.id, index);
                setProgressFooter(wrapperPracticeFooter, rule.id);
                mListFooterRect.add(wrapperPracticeFooter);

                mLinerLayoutPractices.addView(wrapperPractice);
                index++;

            }
        }
        cursor.close();


    }

    private void createPracticesTable() {

        Context context = getContext();
        Activity activity = getActivity();
        if (context == null || activity == null)
            return;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDisplay = displayMetrics.widthPixels;

        int widthCell = DataModule.convertDpToPixel(50, context);
        int heightCell = DataModule.convertDpToPixel(100, context);
        int widthLeftSide = widthDisplay - 4 * widthCell;
        int heightHeader = DataModule.convertDpToPixel(50, context);

        View viewLeftSide = getViewLeftSide(context, widthLeftSide, heightHeader, heightCell);
        View viewCenterSide = getViewCenterSide(context, widthCell, heightHeader, heightCell);
        View viewRightSide = getViewRightSide(context, widthCell, heightHeader, heightCell);

        mLinerLayoutTable.addView(viewLeftSide);
        mLinerLayoutTable.addView(viewCenterSide);
        mLinerLayoutTable.addView(viewRightSide);
    }

    private View getViewLeftSide(Context context, int width, int heightHeader, int heightCell) {

        LinearLayout layoutLeftSide = new LinearLayout(context);
        layoutLeftSide.setOrientation(LinearLayout.VERTICAL);
        layoutLeftSide.setLayoutParams(new FrameLayout.LayoutParams(width,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        layoutLeftSide.setBackground(context.getDrawable(R.drawable.cell_shape));

        TextView textViewTitle = new TextView(context);
        textViewTitle.setText("Мои правила");
        textViewTitle.setTypeface(null, Typeface.BOLD);
        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textViewTitle.setGravity(Gravity.CENTER);
        textViewTitle.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                heightHeader));
        textViewTitle.setBackground(context.getDrawable(R.drawable.cell_shape_dark));
        textViewTitle.setTextColor(Color.WHITE);
        layoutLeftSide.addView(textViewTitle);

        int paddingDP = DataModule.convertDpToPixel(4, context);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name, r.done" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id GROUP BY r._id ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.name = cursor.getString(1);
                rule.done = cursor.getInt(2);

                TextView textViewRule = new TextView(context);
                textViewRule.setText(rule.name);
                textViewRule.setHeight(heightCell);
                textViewRule.setGravity(Gravity.CENTER);
                textViewRule.setEllipsize(TextUtils.TruncateAt.END);
                textViewRule.setMaxLines(4);
                textViewRule.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textViewRule.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        heightCell));
                textViewRule.setPadding(paddingDP, 0, paddingDP, 0);
                textViewRule.setBackground(context.getDrawable(R.drawable.cell_shape_light_red));
                if (rule.done == 1)
                    textViewRule.setBackground(context.getDrawable(R.drawable.cell_shape_light_orange));
                if (rule.done == 2)
                    textViewRule.setBackground(context.getDrawable(R.drawable.cell_shape_light_green));

                layoutLeftSide.addView(textViewRule);
            }
        }

        return layoutLeftSide;
    }


    private View getViewCenterSide(Context context, int widthCell, int heightHeader, int heightCell) {

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(widthCell,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        View view = new View(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(widthCell, heightHeader));
        view.setBackground(context.getDrawable(R.drawable.cell_shape_dark));
        layout.addView(view);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name, r.done" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id GROUP BY r._id ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                TextView viewCell = new TextView(context);
                viewCell.setTag(cursor.getInt(0));
                viewCell.setGravity(Gravity.CENTER);
                viewCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                viewCell.setTypeface(null, Typeface.BOLD);
                viewCell.setLayoutParams(new FrameLayout.LayoutParams(widthCell, heightCell));
                viewCell.setBackground(context.getDrawable(R.drawable.cell_shape_violet));
                layout.addView(viewCell);
                listBadgesTable.add(viewCell);
            }
        }
        refreshBadgesTable();

        return layout;
    }

    private View getViewRightSide(Context context, int widthCell, int heightHeader, int heightCell) {

        HorizontalScrollView scrollView = new HorizontalScrollView(context);
        scrollView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setBackground(context.getDrawable(R.drawable.cell_shape_yellow));
        scrollView.addView(layout);

        int paddingDP = DataModule.convertDpToPixel(6, context);

        // заголовок
        LinearLayout layoutHeader = new LinearLayout(context);
        layoutHeader.setOrientation(LinearLayout.HORIZONTAL);
        layoutHeader.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, heightHeader));
        layout.addView(layoutHeader);

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT MIN(p.date) min, MAX(p.date) max" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id", null);
        if ((cursor != null) && cursor.moveToFirst()) {

            int minDate = cursor.getInt(0);
            int maxDate = cursor.getInt(1);

            for (int i = minDate; i < maxDate + 1; i++) {
                TextView viewCell = new TextView(context);
                viewCell.setLayoutParams(new FrameLayout.LayoutParams(widthCell, heightHeader));
                viewCell.setBackground(context.getDrawable(R.drawable.cell_shape_dark));

                long dateInt = (long) i * 1000 * 86400;
                SimpleDateFormat fmt = new SimpleDateFormat("dd.MM");
                String dateStr = fmt.format(dateInt);
                viewCell.setGravity(Gravity.CENTER);
                viewCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                viewCell.setTextColor(Color.WHITE);
                viewCell.setText(dateStr);

                layoutHeader.addView(viewCell);
            }

            Cursor cursorP = DataModule.dbReader.rawQuery("SELECT r._id FROM rule r " +
                    "JOIN practice p ON r._id = p.rule_id GROUP BY r._id ORDER BY p._id", null);

            if ((cursorP != null)) {
                while (cursorP.moveToNext()) {
                    LinearLayout layoutRule = new LinearLayout(context);
                    layoutRule.setOrientation(LinearLayout.HORIZONTAL);
                    layoutRule.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, heightCell));

                    Cursor cursorL = DataModule.dbReader.rawQuery("SELECT p._id, p.result, p.done, p.date" +
                            " FROM practice p WHERE p.rule_id = ?", new String[]{cursorP.getString(0)});
                    if (cursorL != null) {
                        for (int i = minDate; i < maxDate + 1; i++) {
                            RelativeLayout view = new RelativeLayout(context);
                            view.setBackground(context.getDrawable(R.drawable.cell_shape_dark));
                            cursorL.moveToFirst();
                            while (true) {
                                if (i == cursorL.getInt(3)) {

                                    RuleInfo info = new RuleInfo();
                                    info.id = cursorP.getInt(0);
                                    info.practiceId = cursorL.getInt(0);
                                    info.status = cursorL.getInt(1);
                                    info.done = cursorL.getInt(2);

                                    view.setBackground(context.getDrawable(R.drawable.cell_shape_orange));
                                    ImageView image = new ImageView(context);
                                    image.setImageDrawable(context.getDrawable(R.drawable.ic_box));
                                    if (cursorL.getInt(2) == 1) {
                                        if (cursorL.getInt(1) == 1)
                                            image.setImageDrawable(context.getDrawable(R.drawable.ic_check_gray));
                                        else
                                            image.setImageDrawable(context.getDrawable(R.drawable.ic_unchecked));
                                    }
                                    image.setPadding(paddingDP, paddingDP, paddingDP, paddingDP);
                                    image.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT));
                                    image.setTag(info);
                                    view.addView(image);
                                    image.setOnClickListener(v -> {
                                        ImageView imageL = (ImageView) v;
                                        RuleInfo infoL = (RuleInfo) v.getTag();
                                        if (infoL.done == 0) {
                                            infoL.done = 1;
                                            infoL.status = 1;
                                            imageL.setImageDrawable(context.getDrawable(R.drawable.ic_check_gray));
                                            updatePractice(infoL);
                                            return;
                                        }
                                        if ((infoL.status == 0) && (infoL.done == 1)) {
                                            infoL.done = 0;
                                            image.setImageDrawable(context.getDrawable(R.drawable.ic_box));
                                            updatePractice(infoL);
                                            return;
                                        }
                                        imageL.setImageDrawable(context.getDrawable(R.drawable.ic_unchecked));
                                        infoL.status = 0;
                                        updatePractice(infoL);
                                    });

                                    break;
                                }
                                if (!cursorL.moveToNext())
                                    break;
                            }
                            view.setLayoutParams(new FrameLayout.LayoutParams(widthCell, heightCell));
                            layoutRule.addView(view);
                        }
                    }
                    layout.addView(layoutRule);

                }
            }

        }

        return scrollView;

    }

    private void refreshBadgesTable() {

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, SUM(p.result), SUM(p.done)" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id GROUP BY r._id ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                for (int i = 0; i < listBadgesTable.size(); i++) {
                    TextView textView = listBadgesTable.get(i);
                    if ((int) textView.getTag() == cursor.getInt(0)) {
                        String s = cursor.getInt(1) + "/" + cursor.getInt(2);
                        textView.setText(s);
                        break;
                    }
                }
            }
        }
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
                if (mMapPractice.get(info.id) == null) {
                    days = new RuleInfo[21];
                    mMapPractice.put(info.id, days);
                    index = 0;
                    mMapCountDays.put(info.id, index);
                } else {
                    days = mMapPractice.get(info.id);
                    index = mMapCountDays.get(info.id);
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
                    mMapCountDays.put(info.id, index);
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
        RuleInfo[] days = new RuleInfo[21];
        if (mMapPractice.get(ruleID) != null)
            days = mMapPractice.get(ruleID);
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
                    if (days[pos].date <= sCurrentDate) {
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
                    }
                    sListRect.add(rect);
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
        if ((mMapPractice != null) && (mMapPractice.get(ruleID) != null))
            badge.setText(String.valueOf(getCountSuccessDays(mMapPractice.get(ruleID))));
        badge.setTextSize(textSize);
        badge.setTextColor(Color.WHITE);
        badge.setTag(ruleID);
        sListBadges.add(badge);

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
                View practice = sListPractice.get(i);
                View footer = mListFooterRect.get(i);
                View main = mListMainRect.get(i);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) practice.getLayoutParams();

                if (footer.getVisibility() == View.GONE) {
                    footer.setVisibility(View.VISIBLE);
                    main.setVisibility(View.GONE);
                    layoutParams.height = height * 2;
                    ((ImageButton) v).setImageResource(R.drawable.ic_remove_30_white);
                } else {
                    footer.setVisibility(View.GONE);
                    main.setVisibility(View.VISIBLE);
                    layoutParams.height = height;
                    ((ImageButton) v).setImageResource(R.drawable.ic_add_30_white);
                }
                practice.setLayoutParams(layoutParams);
            }
        });

        LinearLayout wrapperRect = new LinearLayout(context);
        wrapperRect.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsW = new LinearLayout.LayoutParams(widthL, height);
        wrapperRect.setLayoutParams(layoutParamsW);
        mListMainRect.add(wrapperRect);

        int h;
        int pos = 0;
        RuleInfo[] days = new RuleInfo[21];
        if (mMapPractice.get(ruleID) != null)
            days = mMapPractice.get(ruleID);
        if (days != null) {
            for (int d = 0; d < days.length; d++) {
                if (days[d].date == sCurrentDate) {
                    pos = d - 5;
                    if (pos < 0)
                        pos = 0;
                    break;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            LinearLayout wrapperH = new LinearLayout(context);
            wrapperH.setOrientation(LinearLayout.HORIZONTAL);
            h = (i == 2) ? ((height / 3) + size1) : (height / 3);
            wrapperH.setLayoutParams(new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, h));
            wrapperH.setWeightSum(2);

            for (int j = 0; j < 2; j++) {
                TextView rect = new TextView(context);
                rect.setGravity(Gravity.CENTER);
                rect.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                rect.setLayoutParams(new LinearLayout.LayoutParams(0, h, 1));
                rect.setBackground(context.getDrawable(R.drawable.cell_shape_light_gray));
                if ((days != null) && (pos < days.length) && (days[pos] != null)) {
                    long dateInt = (long) days[pos].date * 1000 * 86400;
                    SimpleDateFormat fmt = new SimpleDateFormat("E");
                    String dateStr = fmt.format(dateInt);
                    rect.setText(dateStr);
                    if (days[pos].date <= sCurrentDate) {
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
                    }
                    sListRect.add(rect);
                }
                wrapperH.addView(rect);
                pos++;
            }

            wrapperRect.addView(wrapperH);
        }

        layout.addView(wrapperRect);
    }

    public static void updateStatuses(Context context) {

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id GROUP BY r._id ORDER BY p._id", null);

        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                RuleInfo info = new RuleInfo();
                info.id = cursor.getInt(0);
                info.name = cursor.getString(1);
                updateStatusRule(info);
                checkRuleOnFinish(context, info);
            }
        }
    }

    private static void checkRuleOnFinish(Context context, RuleInfo info) {
        boolean isLast = checkRuleIsLast(info);
        if (!isLast)
            return;

        AlertDialog.Builder ad;
        String title = "Практика " + info.name + " завершена!";
        String message = "Удалить правило из практики?";
        String buttonYesString = "Да";
        String buttonNoString = "Нет";

        ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setPositiveButton(buttonYesString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                DataModule.dbWriter.execSQL("DELETE FROM practice WHERE rule_id = " + info.id);
                DataModule.dbWriter.execSQL("UPDATE rule SET checked = 0 WHERE _id = " + info.id);
                try {
                    for (int i = 0; i < PracticeFragment.sListPractice.size(); i++) {
                        if ((int) PracticeFragment.sListPractice.get(i).getTag() == info.id) {
                            PracticeFragment.sListPractice.get(i).setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {

                }

            }
        });
        ad.setNegativeButton(buttonNoString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                Cursor cursor = DataModule.dbReader.rawQuery(
                        "SELECT MIN(_id) FROM practice WHERE rule_id = ?",
                        new String[]{String.valueOf(info.id)});

                if ((cursor != null) && cursor.moveToFirst()) {

                    int id = cursor.getInt(0);

                    ContentValues cv = new ContentValues();
                    Date currentTime = Calendar.getInstance().getTime();
                    int startDate = (int) (currentTime.getTime() / (1000 * 86400));
                    int endDate = startDate + 20;
                    for (int i = startDate; i <= endDate; i++) {
                        cv.clear();
                        cv.put("date", i);
                        cv.put("done", 0);
                        cv.putNull("result");
                        DataModule.dbWriter.update("practice", cv, "rule_id = ? AND _id = ?",
                                new String[]{String.valueOf(info.id), String.valueOf(id)});
                        id++;
                    }
                    updateRectViews();
                }

            }
        });
        ad.setCancelable(false);
        ad.show();
    }

    private static boolean checkRuleIsLast(RuleInfo info) {
        Cursor cursor = DataModule.dbReader.rawQuery(
                "SELECT COUNT(_id) FROM practice WHERE rule_id = ? AND done = 1",
                new String[]{String.valueOf(info.id)});
        if ((cursor != null) && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            return count == Consts.COUNT_PRACTICES;
        }
        return false;
    }

    private static void updateStatusRule(RuleInfo info) {
        Cursor cursor = DataModule.dbReader.rawQuery(
                "SELECT COUNT(_id) FROM practice WHERE result = 1 AND rule_id = ?",
                new String[]{String.valueOf(info.id)});
        if ((cursor != null) && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 17)
                DataModule.dbWriter.execSQL("UPDATE rule SET done = 1 WHERE done = 0 AND _id = " + info.id);
            if (count > 19)
                DataModule.dbWriter.execSQL("UPDATE rule SET done = 2 WHERE _id = " + info.id);
        }
    }

    private void updatePractice(RuleInfo info) {

        ContentValues cv = new ContentValues();
        cv.put("result", info.status);
        cv.put("done", info.done);
        DataModule.dbWriter.update("practice", cv, "_id = ?", new String[]{String.valueOf(info.practiceId)});

        refreshBadgesTable();
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
        mBinding = null;
    }
}