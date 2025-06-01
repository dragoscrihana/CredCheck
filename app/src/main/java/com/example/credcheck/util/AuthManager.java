package com.example.credcheck.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import java.util.function.BiConsumer;

public class AuthManager {

    private static final String SECURE_PREFS_NAME = "credcheck_secure_prefs";
    private static final String AUTH_STATE_KEY = "auth_state";

    private static SharedPreferences getSecurePrefs(Context context) {
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    SECURE_PREFS_NAME,
                    masterKey,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e("AuthManager", "Failed to get EncryptedSharedPreferences", e);
            return context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public static AuthState loadAuthState(Context context) {
        SharedPreferences prefs = getSecurePrefs(context);
        String stateJson = prefs.getString(AUTH_STATE_KEY, null);

        if (stateJson != null) {
            try {
                return AuthState.jsonDeserialize(stateJson);
            } catch (Exception e) {
                Log.e("AuthManager", "Failed to deserialize AuthState", e);
            }
        }

        return new AuthState();
    }

    public static void persistAuthState(Context context, AuthState state) {
        try {
            getSecurePrefs(context).edit()
                    .putString(AUTH_STATE_KEY, state.jsonSerializeString())
                    .apply();
        } catch (Exception e) {
            Log.e("AuthManager", "Failed to persist AuthState", e);
        }
    }

    public static void getFreshAccessToken(Context context, BiConsumer<String, AuthState> callback) {
        AuthState authState = loadAuthState(context);
        AuthorizationService service = new AuthorizationService(context);

        if (authState.getNeedsTokenRefresh() && authState.getRefreshToken() == null) {
            Log.e("AuthManager", "No refresh token available and token is expired");
            callback.accept(null, authState);
            return;
        }

        authState.performActionWithFreshTokens(service, (accessToken, idToken, ex) -> {
            if (ex != null || accessToken == null) {
                Log.e("AuthManager", "Token refresh failed", ex);
                callback.accept(null, authState);
            } else {
                callback.accept(accessToken, authState);
            }
        });
    }

    public static void clearAuthState(Context context) {
        getSecurePrefs(context).edit().clear().apply();
    }
}
