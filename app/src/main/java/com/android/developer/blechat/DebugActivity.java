package com.android.developer.blechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        setupComponents();
    }

    private void setupComponents() {

        Button waitingButton = findViewById(R.id.waiting_button);

        findViewById(R.id.loading_panel).setVisibility(View.GONE);

        waitingButton.setOnClickListener(v -> handleWaitingButtonOnClick());

    }

    private void handleWaitingButtonOnClick() {
        findViewById(R.id.loading_panel).setVisibility(View.VISIBLE);
    }
}
