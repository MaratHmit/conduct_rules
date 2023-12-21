package ru.am.conduct_rules;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import ru.am.conduct_rules.ui.MainActivity;

public class StartLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_login);

        ImageButton buttonApply = findViewById(R.id.btn_apply);
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply();
            }
        });

    }

    private void apply() {

        Context context = this.getBaseContext();

        if (DataModule.dbWriter == null)
            DataModule.initDBHelper(context);

        EditText editTextName = (EditText) findViewById(R.id.editTextName);
        EditText editTextAge = (EditText) findViewById(R.id.editTextAge);
        EditText editTextMeditation = (EditText) findViewById(R.id.editTextMeditation);

        if ((editTextName == null) || (editTextAge == null) || (editTextMeditation == null))
            return;

        ContentValues cv = new ContentValues();
        cv.put("name", editTextName.getText().toString());
        cv.put("age", editTextAge.getText().toString());
        cv.put("meditation", editTextMeditation.getText().toString());
        cv.put("start", 1);
        DataModule.dbWriter.update("user", cv, "_id = 1", null);

        Intent intent = new Intent(StartLoginActivity.this, EstimateActivity.class);
        startActivity(intent);
        finish();
    }
}
