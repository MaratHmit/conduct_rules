package ru.am.conduct_rules;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
        mTextRuleCode.setText(mListRules.get(mCurrentIndex).code);
        mTextRuleIndex.setText(String.valueOf(mCurrentIndex + 1) + "/" + mListRules.size());

        if (mCurrentIndex > 0)
            mButtonBack.setVisibility(View.VISIBLE);
    }

    private Drawable getColorByEstimate(int estimate) {

        switch (estimate) {
            case 1:
                return getDrawable(R.drawable.cell_shape_light_red);
            case 2:
                return getDrawable(R.drawable.cell_shape_light_orange);
            case 3:
                return getDrawable(R.drawable.cell_shape_light_green);
        }

        return getDrawable(R.drawable.cell_shape_gray);
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
            mButtonBack.setVisibility(View.INVISIBLE);
        }
        updateRuleInfo();
    }

    private void saveRuleEstimate(int tag) {

        int id = mListRules.get(mCurrentIndex - 1).id;
        mListRules.get(mCurrentIndex - 1).estimate = tag;
        DataModule.dbWriter.execSQL("UPDATE rule SET estimate = " + tag + " WHERE _id = " + id);
    }


}
