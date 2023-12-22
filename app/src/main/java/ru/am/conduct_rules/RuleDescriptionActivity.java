package ru.am.conduct_rules;

import android.app.Activity;
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
        btnBack.setOnClickListener(view -> {finish();});


    }

    private void setDataToControls() {

        Bundle arguments = getIntent().getExtras();
        if (arguments == null)
            return;;

        String title = arguments.get("title").toString();
        String description = arguments.get("description").toString();

        TextView textViewTitle = findViewById(R.id.tv_practiceName);
        if (textViewTitle != null)
            textViewTitle.setText(title);
        TextView textViewDescription = findViewById(R.id.tv_practiceDescription);
        if (textViewDescription != null)
            textViewDescription.setText(description);

    }
}
