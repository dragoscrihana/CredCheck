package com.example.credcheck;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private Spinner themeSpinner;
    private TextView versionText;
    private Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        themeSpinner = root.findViewById(R.id.themeSpinner);
        versionText = root.findViewById(R.id.versionText);
        logoutButton = root.findViewById(R.id.logoutButton);

        // Set version
        versionText.setText("1.0.0");

        // Setup theme spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.theme_options,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        themeSpinner.setAdapter(adapter);

        // Load saved preference
        SharedPreferences prefs = requireContext().getSharedPreferences("credcheck_prefs", Context.MODE_PRIVATE);
        String currentTheme = prefs.getString("theme", "light");
        themeSpinner.setSelection(currentTheme.equals("light") ? 0 : 1);

        // Apply theme change
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
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Log out (just a placeholder for now)
        logoutButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Logged out (simulate)", Toast.LENGTH_SHORT).show()
        );

        return root;
    }
}
