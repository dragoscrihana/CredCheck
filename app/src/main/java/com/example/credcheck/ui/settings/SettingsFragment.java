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
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import net.openid.appauth.AuthState;

public class SettingsFragment extends Fragment implements OnMapReadyCallback {

    private Spinner themeSpinner;
    private TextView versionText;
    private Button logoutButton;
    private GoogleMap map;
    private Marker locationMarker;
    private static final LatLng DEFAULT_LOCATION = new LatLng(44.4268, 26.1025); // Bucharest

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        themeSpinner = root.findViewById(R.id.themeSpinner);
        versionText = root.findViewById(R.id.versionText);
        logoutButton = root.findViewById(R.id.logoutButton);

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

            // Return to LoginActivity
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }, 2000);
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_container);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
        }

        mapFragment.getMapAsync(this);

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        SharedPreferences prefs = requireContext().getSharedPreferences("credcheck_prefs", Context.MODE_PRIVATE);
        double lat = Double.longBitsToDouble(prefs.getLong("location_lat", Double.doubleToLongBits(DEFAULT_LOCATION.latitude)));
        double lng = Double.longBitsToDouble(prefs.getLong("location_lng", Double.doubleToLongBits(DEFAULT_LOCATION.longitude)));
        LatLng storedLocation = new LatLng(lat, lng);

        locationMarker = map.addMarker(new MarkerOptions()
                .position(storedLocation)
                .title("Verifier Location")
                .draggable(true));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(storedLocation, 15f));

        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(Marker marker) {}
            @Override public void onMarkerDrag(Marker marker) {}
            @Override
            public void onMarkerDragEnd(Marker marker) {
                saveLocation(marker.getPosition());
            }
        });

        map.setOnMapClickListener(latLng -> {
            if (locationMarker != null) locationMarker.remove();
            locationMarker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Verifier Location")
                    .draggable(true));
            saveLocation(latLng);
        });
    }

    private void saveLocation(LatLng latLng) {
        SharedPreferences prefs = requireContext().getSharedPreferences("credcheck_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("location_lat", Double.doubleToLongBits(latLng.latitude));
        editor.putLong("location_lng", Double.doubleToLongBits(latLng.longitude));
        editor.apply();
        Toast.makeText(getContext(), "Location updated", Toast.LENGTH_SHORT).show();
    }
}
