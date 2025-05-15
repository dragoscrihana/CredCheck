package com.example.credcheck.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.credcheck.BuildConfig;
import com.example.credcheck.R;
import com.example.credcheck.ui.main.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

public class LoginActivity extends AppCompatActivity {

    private static final String AUTH_ENDPOINT =
            BuildConfig.BASE_URL + "/realms/verifier-realm/protocol/openid-connect/auth";
    private static final String TOKEN_ENDPOINT =
            BuildConfig.BASE_URL + "/realms/verifier-realm/protocol/openid-connect/token";
    private static final String CLIENT_ID = "verifier-mobile";
    private static final String REDIRECT_URI = "com.example.credcheck://callback";
    private static final int RC_AUTH = 1001;

    private AuthorizationService authService;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> startKeycloakLogin());

        forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
            BottomSheetDialog dialog = new BottomSheetDialog(LoginActivity.this);
            dialog.setContentView(dialogView);

            Button okButton = dialogView.findViewById(R.id.okButton);
            okButton.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });
    }

    private void startKeycloakLogin() {
        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(AUTH_ENDPOINT),
                Uri.parse(TOKEN_ENDPOINT)
        );

        AuthorizationRequest request = new AuthorizationRequest.Builder(
                serviceConfig,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(REDIRECT_URI)
        )
                .setScope("openid profile")
                .build();

        authService = new AuthorizationService(this);
        Intent authIntent = authService.getAuthorizationRequestIntent(request);
        startActivityForResult(authIntent, RC_AUTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_AUTH) {
            AuthorizationResponse response = AuthorizationResponse.fromIntent(data);
            AuthorizationException ex = AuthorizationException.fromIntent(data);

            if (response != null) {
                TokenRequest tokenRequest = response.createTokenExchangeRequest();

                authService.performTokenRequest(tokenRequest, (tokenResponse, exception) -> {
                    if (tokenResponse != null) {
                        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                                Uri.parse(AUTH_ENDPOINT),
                                Uri.parse(TOKEN_ENDPOINT)
                        );

                        AuthState authState = new AuthState(serviceConfig);
                        authState.update(response, ex);
                        authState.update(tokenResponse, null);

                        persistAuthState(authState);

                        Log.d("ACCESS_TOKEN", tokenResponse.accessToken);
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Token exchange failed", Toast.LENGTH_SHORT).show();
                        Log.e("TOKEN_EXCHANGE", "Error", exception);
                    }
                });
            } else {
                Toast.makeText(this, "Authorization failed", Toast.LENGTH_SHORT).show();
                Log.e("AUTH", "Authorization failed", ex);
            }
        }
    }

    private void persistAuthState(AuthState authState) {
        SharedPreferences prefs = getSharedPreferences("credcheck_prefs", MODE_PRIVATE);
        prefs.edit().putString("auth_state", authState.jsonSerializeString()).apply();
    }
}
