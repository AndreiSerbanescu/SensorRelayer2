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

//    private void setupComponents() {
//        Button waitingButton = findViewById(R.id.waiting_button);
//        Button lightSensorButton = findViewById(R.id.light_sensor_button);
//
//        LottieAnimationView loadingAV = initAnimation(R.id.loading_animation);
//        LottieAnimationView lightSensorAV = initAnimation(R.id.light_sensor_animation);
//
//        loadingAV.setVisibility(View.VISIBLE);
//        loadingAV.playAnimation();
//
//        waitingButton.setOnClickListener(v -> handleWaitingButtonOnClick(loadingAV));
//        lightSensorButton.setOnClickListener(v -> handleLightButtonClick(lightSensorAV));
//    }

    private void setupComponents() {
        Button waitingButton = findViewById(R.id.waiting_button);
        Button lightSensorButton = findViewById(R.id.light_sensor_button);


        LottieAnimationView lightSensorAV = findViewById(R.id.light_sensor_animation);
        lightSensorAV.setVisibility(View.INVISIBLE);

        lightSensorAV.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                lightSensorAV.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lightSensorAV.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                lightSensorAV.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        lightSensorAV.setOnClickListener(v -> lightSensorAV.cancelAnimation());

        lightSensorButton.setOnClickListener(v -> lightSensorAV.playAnimation());


        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        loadingAnimation.setVisibility(View.INVISIBLE);

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

    private void handleWaitingButtonOnClick(LottieAnimationView loadingAnimationView) {
        //loadingAnimationView.setVisibility(View.VISIBLE);
        //loadingAnimationView.playAnimation();

        loadingAnimationView.cancelAnimation();
        loadingAnimationView.setVisibility(View.INVISIBLE);
    }

    private void handleLightButtonClick(LottieAnimationView lightSensorAV) {
        lightSensorAV.setVisibility(View.VISIBLE);
        lightSensorAV.playAnimation();
    }

    private LottieAnimationView initAnimation(int animationID) {

        LottieAnimationView animationView = findViewById(animationID);
        animationView.setVisibility(View.GONE);

        return animationView;
    }

}
