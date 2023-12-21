package ru.am.conduct_rules;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.am.conduct_rules.ui.MainActivity;

public class EstimateActivity extends AppCompatActivity {

    private ArrayList<RuleInfo> mListRules;
    private TextView mTextRuleName;
    private TextView mTextRuleCode;
    private TextView mTextRuleIndex;
    private int mCurrentIndex;
    private Button mButtonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.estimate_layout);

        mListRules = new ArrayList<>();
        mTextRuleName = findViewById(R.id.textViewRuleEstimate);
        mTextRuleCode = findViewById(R.id.textViewCodeRule);
        mTextRuleIndex = findViewById(R.id.textViewIndexRule);
        mButtonBack = findViewById(R.id.buttonBackM);

        Button buttonBack = findViewById(R.id.buttonBackEstimate);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                if (DataModule.isFirstStart) {
                    DataModule.isFirstStart = false;
                    Intent intent = new Intent(EstimateActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        loadRules();
        updateRuleInfo();
    }

    private void updateRuleInfo() {

        if (mTextRuleName == null || mListRules.size() == 0)
            return;

        mTextRuleName.setText(mListRules.get(mCurrentIndex).name);
        mTextRuleName.setBackground(getColorByEstimate(mListRules.get(mCurrentIndex).estimate));
        if (mListRules.get(mCurrentIndex).estimate > 0)
            mTextRuleName.setTextColor(Color.WHITE);
        else
            mTextRuleName.setTextColor(Color.BLACK);
        mTextRuleCode.setText(mListRules.get(mCurrentIndex).code);
        mTextRuleIndex.setText(String.valueOf(mCurrentIndex + 1) + "/" + mListRules.size());

        if (mCurrentIndex > 0)
            mButtonBack.setVisibility(View.VISIBLE);
    }

    private Drawable getColorByEstimate(int estimate) {

        switch (estimate) {
            case 1:
                return getDrawable(R.drawable.cell_shape_light_red_corner);
            case 2:
                return getDrawable(R.drawable.cell_shape_light_orange_corner);
            case 3:
                return getDrawable(R.drawable.cell_shape_light_green_corner);
        }

        return getDrawable(R.drawable.cell_shape_gray_corner);
    }

    private void loadRules() {

        Cursor cursor = DataModule.dbReader.rawQuery("SELECT _id, code, name, estimate FROM rule ORDER BY _id", null);
        if ((cursor != null)) {
            while (cursor.moveToNext()) {
                RuleInfo info = new RuleInfo();
                info.id = cursor.getInt(0);
                info.code = "Пункт № " + cursor.getString(1);
                info.name = cursor.getString(2);
                info.estimate = cursor.getInt(3);
                mListRules.add(info);
            }
        }
    }

    public void onButtonClickEstimate(View view) {
        mCurrentIndex++;
        mButtonBack.setEnabled(true);
        if (mCurrentIndex == mListRules.size()) {

            Toast toast = Toast.makeText(this, "Оценка правил завершена!", Toast.LENGTH_SHORT);
            toast.show();

            finish();
            return;
        }
        updateRuleInfo();
        if (view.getTag() != null) {
            saveRuleEstimate(Integer.parseInt(view.getTag().toString()));
        }
    }

    public void onButtonClickBack(View view) {
        mCurrentIndex--;
        if (mCurrentIndex <= 0) {
            mCurrentIndex = 0;
            mButtonBack.setEnabled(false);
        }
        updateRuleInfo();
    }

    private void saveRuleEstimate(int tag) {

        int id = mListRules.get(mCurrentIndex - 1).id;
        mListRules.get(mCurrentIndex - 1).estimate = tag;
        DataModule.dbWriter.execSQL("UPDATE rule SET estimate = " + tag + " WHERE _id = " + id);

        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.ok);
        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ok);
            }
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
