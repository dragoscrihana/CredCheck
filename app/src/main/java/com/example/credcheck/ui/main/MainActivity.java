package com.example.credcheck.ui.main;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.credcheck.ui.history.HistoryFragment;
import com.example.credcheck.R;
import com.example.credcheck.ui.settings.SettingsFragment;
import com.example.credcheck.ui.verify.VerifyFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);

        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_verify) {
                selectedFragment = new VerifyFragment();
            } else if (item.getItemId() == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (item.getItemId() == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            navView.setSelectedItemId(R.id.nav_verify);
        }
    }

    public void showOverlay(boolean isAccepted) {
        View dimOverlay = findViewById(R.id.globalDimOverlay);
        LinearLayout overlayContainer = findViewById(R.id.globalOverlayContainer);
        LottieAnimationView animationView = findViewById(R.id.globalAnimationView);
        TextView statusText = findViewById(R.id.globalStatusText);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        dimOverlay.setVisibility(View.VISIBLE);
        overlayContainer.setVisibility(View.VISIBLE);

        if (isAccepted) {
            animationView.setAnimation(R.raw.success);
            statusText.setText("Accepted");
        } else {
            animationView.setAnimation(R.raw.failed);
            statusText.setText("Denied");
        }

        animationView.setScaleX(1f);
        animationView.setScaleY(1f);
        animationView.setAlpha(1f);
        animationView.setTranslationX(0f);
        animationView.setTranslationY(0f);
        statusText.setAlpha(1f);

        animationView.playAnimation();

        new Handler().postDelayed(() -> {
            View historyItem = bottomNav.findViewById(R.id.bottom_navigation);
            if (historyItem != null) {
                int[] fromLoc = new int[2];
                int[] toLoc = new int[2];

                animationView.getLocationOnScreen(fromLoc);
                historyItem.getLocationOnScreen(toLoc);

                float fromCenterX = fromLoc[0] + animationView.getWidth() / 2f;
                float fromCenterY = fromLoc[1] + animationView.getHeight() / 2f;
                float toCenterX = toLoc[0] + historyItem.getWidth() / 2f;
                float toCenterY = toLoc[1] + historyItem.getHeight() / 2f;

                float deltaX = toCenterX - fromCenterX;
                float deltaY = toCenterY - fromCenterY;

                statusText.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start();

                animationView.animate()
                        .scaleX(0.2f)
                        .scaleY(0.2f)
                        .translationX(deltaX)
                        .translationY(deltaY)
                        .alpha(0f)
                        .setDuration(800)
                        .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                        .withEndAction(() -> {
                            dimOverlay.setVisibility(View.GONE);
                            overlayContainer.setVisibility(View.GONE);
                            animationView.setScaleX(1f);
                            animationView.setScaleY(1f);
                            animationView.setTranslationX(0f);
                            animationView.setTranslationY(0f);
                            animationView.setAlpha(1f);
                            statusText.setAlpha(1f);
                        })
                        .start();
            }
        }, 2000);

        overlayContainer.setOnClickListener(v -> {
            dimOverlay.setVisibility(View.GONE);
            overlayContainer.setVisibility(View.GONE);
        });
    }




}
