package com.example.credcheck.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;

import java.util.function.BiConsumer;

public class AuthManager {

    private static final String PREFS_NAME = "credcheck_prefs";
    private static final String AUTH_STATE_KEY = "auth_state";

    public static AuthState loadAuthState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(AUTH_STATE_KEY, state.jsonSerializeString()).apply();
    }

    public static void getFreshAccessToken(Context context, BiConsumer<String, AuthState> callback) {
        AuthState authState = loadAuthState(context);
        AuthorizationService service = new AuthorizationService(context);

        authState.performActionWithFreshTokens(service, (accessToken, idToken, ex) -> {
            if (ex != null || accessToken == null) {
                Log.e("AuthManager", "Token refresh failed", ex);
                callback.accept(null, authState);
            } else {
                callback.accept(accessToken, authState);
            }
        });
    }
}
