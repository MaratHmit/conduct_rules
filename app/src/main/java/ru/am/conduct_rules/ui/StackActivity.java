package ru.am.conduct_rules.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

//import com.daprlabs.cardstack.SwipeDeck;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleCalendar;
import ru.am.conduct_rules.RuleInfo;
import ru.am.conduct_rules.cardstack.SwipeDeck;

public class StackActivity extends AppCompatActivity {

    private SwipeDeck mCardStack;
    private SwipeDeckAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stack);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        LinearLayout backGroundLayout = findViewById(R.id.ll_background);
        mCardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        mCardStack.setBackgroundLayout(backGroundLayout);
        init();
    }

    private void playSound(boolean success) {

        int res = success ? R.raw.ok : R.raw.cancel;

        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), res);
        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp = MediaPlayer.create(getApplicationContext(), res);
            }
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {

        RuleCalendar calendar = new RuleCalendar(getBaseContext());
        LinearLayout llWrapper = findViewById(R.id.ll_wrapper_rule_calendar);
        llWrapper.addView(calendar);

        final ArrayList<RuleInfo> listRules = new ArrayList<>();
        loadRules(listRules);
        if (listRules.size() > 0)
            calendar.update(listRules.get(0).id);

        mAdapter = new SwipeDeckAdapter(listRules, this);
        mCardStack.setAdapter(mAdapter);

        mCardStack.setEventCallback(new SwipeDeck.SwipeEventCallback() {
            @Override
            public void cardSwipedLeft(int position) {
                updatePractice(position, true); // правило выполнено
                playSound(true);
            }

            @Override
            public void cardSwipedRight(int position) {
                updatePractice(position, false); // правило не выполнено
                playSound(false);
            }

            @Override
            public void cardsDepleted() {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void cardActionDown() {
            }

            @Override
            public void cardActionUp() {

            }
        });
    }

    private void updatePractice(int position, boolean check) {

        RuleInfo info = mAdapter.getRuleInfo(position);

        ContentValues cv = new ContentValues();
        cv.put("rule_id", info.id);
        cv.put("result", check ? 1 : 0);
        cv.put("done", 1);
        DataModule.dbWriter.update("practice", cv, "_id = ?", new String[]{String.valueOf(info.practiceId)});
    }

    private void loadRules(ArrayList<RuleInfo> listRules) {

        Date currentTime = Calendar.getInstance().getTime();
        int date = (int) (currentTime.getTime() / (1000 * 86400));
        String strDate = String.valueOf(date);

        String sqlStr = "SELECT p._id AS practice_id, r._id, r.code, r.name, r.title" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id WHERE p.done = 0 AND" +
                " p.date = " + strDate +
                " GROUP BY r._id";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int id = extras.getInt("practiceID");
            if (id > 0)
                sqlStr = "SELECT p._id AS practice_id, r._id, r.code, r.name, r.title" +
                        " FROM rule r JOIN practice p ON r._id = p.rule_id" +
                        " WHERE p._id = " + id;
        }

        Cursor cursor = DataModule.dbReader.rawQuery(sqlStr, null);

        if ((cursor != null)) {
            while (cursor.moveToNext()) {

                RuleInfo rule = new RuleInfo();
                rule.practiceId = cursor.getInt(0);
                rule.id = cursor.getInt(1);
                rule.code = cursor.getString(2);
                rule.name = cursor.getString(3);
                rule.title = cursor.getString(4);
                listRules.add(rule);

            }
        }

    }

    public void onButtonCheckRuleClick(View view) {
        mCardStack.swipeTopCardLeft(10);
    }

    public void onButtonUnCheckRuleClick(View view) {
        mCardStack.swipeTopCardRight(10);
    }

}