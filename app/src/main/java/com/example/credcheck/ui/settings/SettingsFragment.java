package com.example.credcheck.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import com.example.credcheck.BuildConfig;
import com.example.credcheck.R;
import com.example.credcheck.ui.auth.LoginActivity;

import net.openid.appauth.AuthState;

public class SettingsFragment extends Fragment {

    private Spinner themeSpinner;
    private TextView versionText;
    private Button logoutButton;
    private Switch biometricSwitch;
    private TextView biometricStatusText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        themeSpinner = root.findViewById(R.id.themeSpinner);
        versionText = root.findViewById(R.id.versionText);
        logoutButton = root.findViewById(R.id.logoutButton);
        biometricSwitch = root.findViewById(R.id.biometricSwitch);
        biometricStatusText = root.findViewById(R.id.biometricStatusText); // 🆕

        versionText.setText("1.0.0");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.theme_options,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        themeSpinner.setAdapter(adapter);

        SharedPreferences prefs = requireContext().getSharedPreferences("credcheck_prefs", Context.MODE_PRIVATE);
        String currentTheme = prefs.getString("theme", "light");
        themeSpinner.setSelection(currentTheme.equals("light") ? 0 : 1);

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor editor = prefs.edit();

                if (selected.equals("Light")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putString("theme", "light");
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putString("theme", "dark");
                }
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        boolean biometricEnabled = prefs.getBoolean("biometric_enabled", false);
        biometricSwitch.setChecked(biometricEnabled);
        biometricStatusText.setText(biometricEnabled ? "On" : "Off");

        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("biometric_enabled", isChecked).apply();
            biometricStatusText.setText(isChecked ? "On" : "Off");
            Toast.makeText(requireContext(),
                    isChecked ? "Biometric login enabled" : "Biometric login disabled",
                    Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences shared_prefs = requireContext().getSharedPreferences("credcheck_prefs", Context.MODE_PRIVATE);
            String authStateJson = shared_prefs.getString("auth_state", null);
            String idToken = null;

            if (authStateJson != null) {
                try {
                    AuthState authState = AuthState.jsonDeserialize(authStateJson);
                    idToken = authState.getIdToken();
                } catch (Exception e) {
                    Log.e("SettingsFragment", "Failed to load AuthState", e);
                }
            }

            prefs.edit().clear().apply();

            String logoutUrl = BuildConfig.BASE_URL + "/realms/verifier-realm/protocol/openid-connect/logout";
            if (idToken != null) {
                logoutUrl += "?id_token_hint=" + idToken;
            }

            Log.d("Logout URL", logoutUrl);

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(requireContext(), Uri.parse(logoutUrl));

            new Handler().postDelayed(() -> {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }, 2000);
        });

        return root;
    }
}
