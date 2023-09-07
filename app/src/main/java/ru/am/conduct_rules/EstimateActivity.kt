package ru.am.conduct_rules

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class EstimateActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.estimate_layout)

        val buttonClose: Button = findViewById(R.id.buttonBackEstimate)
        buttonClose.setOnClickListener {
            finish()
        }

    }

}