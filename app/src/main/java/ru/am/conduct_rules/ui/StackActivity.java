package ru.am.conduct_rules.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.daprlabs.cardstack.SwipeDeck;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ru.am.conduct_rules.DataModule;
import ru.am.conduct_rules.R;
import ru.am.conduct_rules.RuleInfo;

public class StackActivity extends AppCompatActivity {

    private SwipeDeck cardStack;
    private SwipeDeckAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stack);

        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        init();
    }

    private void init() {

        final ArrayList<RuleInfo> listRules = new ArrayList<>();
        loadRules(listRules);

        mAdapter = new SwipeDeckAdapter(listRules, this);
        cardStack.setAdapter(mAdapter);
        cardStack.setLeftImage(R.id.button_check_rule);
        cardStack.setRightImage(R.id.button_uncheck_rule);

        cardStack.setEventCallback(new SwipeDeck.SwipeEventCallback() {
            @Override
            public void cardSwipedLeft(int position) {
                updatePractice(position, false); // правило не выполнено
            }

            @Override
            public void cardSwipedRight(int position) {
                updatePractice(position, true); // правило выполнено
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
        DataModule.dbWriter.update("practice", cv, "_id = ?", new String[] { String.valueOf(info.practiceId) });
    }

    private void loadRules(ArrayList<RuleInfo> listRules) {

        Date currentTime = Calendar.getInstance().getTime();
        int date = (int) (currentTime.getTime() / (1000 * 86400));
        String strDate = String.valueOf(date);

        String sqlStr = "SELECT p._id AS practice_id, r._id, r.code, r.name" +
                " FROM rule r JOIN practice p ON r._id = p.rule_id WHERE p.done = 0 AND" +
                " p.date = " + strDate +
                " GROUP BY r._id";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int id = extras.getInt("practiceID");
            if (id > 0)
                sqlStr = "SELECT p._id AS practice_id, r._id, r.code, r.name" +
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
                listRules.add(rule);

            }
        }

    }

}