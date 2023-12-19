package ru.am.conduct_rules.ui.practice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import ru.am.conduct_rules.ui.list_rules.ViewRuleItem;

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
    static private ArrayList<TextView> sListBadges;

    static private boolean mIsNightMode;
    private Context mContext;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentPracticeBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        mLinerLayoutPractices = root.findViewById(R.id.listPracticesSwipe);
        mLinerLayoutTable = root.findViewById(R.id.listPracticesTable);
        mScrollViewPractice = root.findViewById(R.id.scrollViewPractice);
        mContext = getContext();

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

        int nightModeFlags = getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        mIsNightMode = Configuration.UI_MODE_NIGHT_YES == nightModeFlags;

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
                        if (info.date <= sCurrentDate)
                            if (rect.getContext() != null)
                                rect.setTextColor(rect.getContext().getColor(R.color.RedText));
                        if (info.done == 1) {
                            switch (info.status) {
                                case 2:
                                    if (rect.getContext() != null)
                                        rect.setTextColor(rect.getContext().getColor(R.color.RedText));
                                    break;
                                case 3:
                                    if (rect.getContext() != null)
                                        rect.setTextColor(rect.getContext().getColor(R.color.GreenText));
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

        for (int i = 0; i < sListBadges.size(); i++) {
            Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, p.result, p.date" +
                            " FROM rule r JOIN practice p ON p.rule_id = r._id WHERE r._id = ?",
                    new String[]{String.valueOf((int) sListBadges.get(i).getTag())});
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
                sListBadges.get(i).setText(String.valueOf(count));
            }
        }
    }

    private void initPractices() {

        if (mMode == 0) {
            mLinerLayoutTable.setVisibility(View.GONE);
            mScrollViewPractice.setVisibility(View.VISIBLE);
            sButtonMarkCards.setVisibility(View.VISIBLE);
            mButtonMode.setText(R.string.table);
            createPracticesSwipe();
            return;
        }

        mLinerLayoutTable.setVisibility(View.VISIBLE);
        mScrollViewPractice.setVisibility(View.GONE);
        sButtonMarkCards.setVisibility(View.GONE);
        mButtonMode.setText(R.string.swipe_mode);
        createPracticesTable();
    }

    private void createPracticesSwipe() {

        Context context = getContext();
        if (context == null)
            return;

        initPracticeList();

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r._id, r.name, r.done, r.code, r.estimate, r.title" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id GROUP BY r._id ORDER BY p._id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.id = cursor.getInt(0);
                rule.name = cursor.getString(1);
                rule.done = cursor.getInt(2);
                rule.code = cursor.getString(3);
                rule.estimate = cursor.getInt(4);
                rule.title = cursor.getString(5);
                rule.available = true;
                rule.mode = 1; // режим практики
                addViewRule(rule);
            }
        }
        cursor.close();
    }

    private void addViewRule(RuleInfo rule) {
        ViewRuleItem item = new ViewRuleItem(mContext, rule);
        mLinerLayoutPractices.addView(item);
        if (item.layoutCalendar != null)
            setProgressFooter(item.layoutCalendar, rule.id);
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
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.d("scrollX", String.valueOf(scrollX));
                Log.d("scrollY", String.valueOf(scrollY));
            }
        });

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

        int paddingX = 0;


        Cursor cursor = DataModule.dbReader.rawQuery("SELECT MIN(p.date) min, MAX(p.date) max" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id", null);
        if ((cursor != null) && cursor.moveToFirst()) {

            int minDate = cursor.getInt(0);
            int maxDate = cursor.getInt(1);

            for (int i = minDate; i < maxDate + 1; i++) {
                TextView viewCell = new TextView(context);
                viewCell.setLayoutParams(new FrameLayout.LayoutParams(widthCell, heightHeader));
                viewCell.setBackground(context.getDrawable(R.drawable.cell_shape_dark));
                if (i == sCurrentDate && paddingX == 0) {
                    paddingX = widthCell * (i - minDate);
                }

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

        if (paddingX >= 0) {
            int finalPaddingX = paddingX;
            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.scrollTo(finalPaddingX, 0);
                }
            });
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
        int height = DataModule.convertDpToPixel(111, context);

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
                Typeface font = ResourcesCompat.getFont(getContext(), R.font.manrope_bold);
                rect.setTypeface(font);
                rect.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                rect.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, h, 1);
                if (j == 0)
                    lp.setMarginStart(size1);
                if (j == 0)
                    rect.setBackground(context.getDrawable(R.drawable.cell_shape_rect_t));
                if (j > 0)
                    rect.setBackground(context.getDrawable(R.drawable.cell_shape_rect_lt));
                if (j == 6) {
                    rect.setBackground(context.getDrawable(R.drawable.cell_shape_rect_lrt));
                    lp.setMarginEnd(size1);
                }
                if (i == 2) {
                    rect.setBackground(context.getDrawable(R.drawable.cell_shape_rect_b));
                }
                if ((j == 6) && (i == 2))
                    rect.setBackground(context.getDrawable(R.drawable.cell_shape_rect_r_corner));
                if ((j == 0) && (i == 2)) {
                    rect.setBackground(context.getDrawable(R.drawable.cell_shape_rect_lb_corner));
                }
                if (i == 1) {
                    rect.setBackground(context.getDrawable(R.drawable.cell_shape_rect_ltb));
                    if (j == 0)
                        rect.setBackground(context.getDrawable(R.drawable.cell_shape_rect_ss));
                }

                rect.setLayoutParams(lp);
                rect.setTextColor(Color.BLACK);
                if (getActivity() != null)
                    rect.setTextColor(getResources().getColor(R.color.GrayText, getActivity().getTheme()));
                if ((days != null) && (pos < days.length) && (days[pos] != null)) {
                    long dateInt = (long) days[pos].date * 1000 * 86400;
                    SimpleDateFormat fmt = new SimpleDateFormat("E dd.MM");
                    String dateStr = fmt.format(dateInt);
                    rect.setText(dateStr);
                    if (days[pos].date <= sCurrentDate) {
                        if (getActivity() != null)
                            rect.setTextColor(getResources().getColor(R.color.RedText, getActivity().getTheme()));
                        if ((days[pos].status == 3) && (getActivity() != null)) {
                            rect.setTextColor(getResources().getColor(R.color.GreenText, getActivity().getTheme()));
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

        if (sCurrentDate == 0) {
            Date currentTime = Calendar.getInstance().getTime();
            sCurrentDate = (int) (currentTime.getTime() / (1000 * 86400));
        }

        boolean isLast = checkRuleIsLast(info);
        if (!isLast)
            return;

        AlertDialog.Builder ad;
        String title = "Практика\n" + info.name + "\nзавершена!";
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
                "SELECT COUNT(_id) FROM practice WHERE date <= ? AND rule_id = ?",
                new String[]{String.valueOf(sCurrentDate), String.valueOf(info.id)});
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
            if (count > 1)
                DataModule.dbWriter.execSQL("UPDATE rule SET estimate = 1 WHERE estimate < 2 AND _id = " + info.id);
            if (count > 17)
                DataModule.dbWriter.execSQL("UPDATE rule SET done = 1, estimate = 2 WHERE estimate < 2 AND _id = " + info.id);
            if (count > 19)
                DataModule.dbWriter.execSQL("UPDATE rule SET done = 2, estimate = 3 WHERE _id = " + info.id);
        }
    }

    private void updatePractice(RuleInfo info) {

        ContentValues cv = new ContentValues();
        cv.put("result", info.status);
        cv.put("done", info.done);
        DataModule.dbWriter.update("practice", cv, "_id = ?", new String[]{String.valueOf(info.practiceId)});

        refreshBadgesTable();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}