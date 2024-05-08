package ru.am.conduct_rules;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

public class RuleDescriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_description);

        setDataToControls();

        Button btnBack = findViewById(R.id.buttonBack);
        btnBack.setOnClickListener(view -> {
            finish();
        });


    }

    private void setDataToControls() {

        Bundle arguments = getIntent().getExtras();
        if (arguments == null)
            return;

        String id = arguments.get("id").toString();
        Cursor cursor = DataModule.dbReader.rawQuery("SELECT r.name, r.description, r.benefits, " +
                "r.instructions, r.links FROM rule r WHERE r._id = ?", new String[]{id});

        if ((cursor != null) && (cursor.moveToFirst())) {

            TextView textViewTitle = findViewById(R.id.tv_practiceName);
            if (textViewTitle != null)
                textViewTitle.setText(cursor.getString(0));

            TextView textViewDescription = findViewById(R.id.tv_practiceDescription);
            if (textViewDescription != null)
                textViewDescription.setText(cursor.getString(1));

            TextView textViewBenefits = findViewById(R.id.tv_practiceBenefits);
            if (textViewBenefits != null)
                textViewBenefits.setText(cursor.getString(2));

            TextView textViewInstructions = findViewById(R.id.tv_practiceInstructions);
            if (textViewInstructions != null)
                textViewInstructions.setText(cursor.getString(3));

        }

    }
}
