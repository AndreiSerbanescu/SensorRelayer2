package com.android.developer.blechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;

public class DebugActivity extends AppCompatActivity {
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        setupComponents();
    }

    private void setupComponents() {
        Button waitingButton = findViewById(R.id.waiting_button);
        Button lightSensorButton = findViewById(R.id.light_sensor_button);

        LottieAnimationView loadingAV = initAnimation(R.id.loading_animation);
        LottieAnimationView lightSensorAV = initAnimation(R.id.light_sensor_animation);

        waitingButton.setOnClickListener(v -> handleWaitingButtonOnClick(loadingAV));
        lightSensorButton.setOnClickListener(v -> handleLightButtonClick(lightSensorAV));
    }

    private void handleWaitingButtonOnClick(LottieAnimationView loadingAnimationView) {
        loadAnimation(loadingAnimationView);
    }

    private void handleLightButtonClick(LottieAnimationView lightSensorAV) {
        loadAnimation(lightSensorAV);
    }

    private void loadAnimation(LottieAnimationView animationView) {
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
    }

    private LottieAnimationView initAnimation(int animationID) {

        LottieAnimationView animationView = findViewById(animationID);
        animationView.setVisibility(View.GONE);

        return animationView;
    }

}
