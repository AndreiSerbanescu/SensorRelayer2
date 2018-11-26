package com.android.developer.blechat;

import android.animation.Animator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;

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
        Button waterSensorButton = findViewById(R.id.water_sensor_button);
        Button sensorActivityButton = findViewById(R.id.sensor_activity);


        sensorActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        LottieAnimationView lightSensorAV = findViewById(R.id.light_sensor_animation);
        lightSensorAV.setVisibility(View.GONE);
        lightSensorAV.addAnimatorListener(new SensorAnimation(lightSensorAV));
        lightSensorAV.setOnClickListener(v -> lightSensorAV.cancelAnimation());
        lightSensorButton.setOnClickListener(v -> lightSensorAV.playAnimation());


        LottieAnimationView waterSensorAV = findViewById(R.id.water_sensor_animation);
        waterSensorAV.setVisibility(View.GONE);
        waterSensorAV.addAnimatorListener(new SensorAnimation(waterSensorAV));
        waterSensorAV.setOnClickListener(v -> waterSensorAV.cancelAnimation());
        waterSensorButton.setOnClickListener(v -> waterSensorAV.playAnimation());


        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        loadingAnimation.setVisibility(View.GONE);

        loadingAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                loadingAnimation.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                loadingAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        loadingAnimation.setOnClickListener(v -> loadingAnimation.cancelAnimation());
        waitingButton.setOnClickListener(v -> loadingAnimation.playAnimation());
    }

    private static class SensorAnimation implements Animator.AnimatorListener {

        private final LottieAnimationView animationView;

        public SensorAnimation(LottieAnimationView animationView) {
            this.animationView = animationView;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            animationView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animationView.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            animationView.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
