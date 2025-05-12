package com.example.credcheck.ui.main;

import android.os.Bundle;
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

        dimOverlay.setVisibility(View.VISIBLE);
        overlayContainer.setVisibility(View.VISIBLE);

        if (isAccepted) {
            animationView.setAnimation(R.raw.success);
            statusText.setText("Accepted");
        } else {
            animationView.setAnimation(R.raw.failed);
            statusText.setText("Denied");
        }

        animationView.playAnimation();

        overlayContainer.setOnClickListener(v -> {
            dimOverlay.setVisibility(View.GONE);
            overlayContainer.setVisibility(View.GONE);
        });
    }

}
