package com.sensimity.android.client.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class to save the authentication tokens of the Sensimity API
 */
public class AuthStorage {

    private Context context;
    private static final String API_PREFS = "sensimity_preferences";
    private static final String API_TOKEN = "sensimity_token";
    private static final String API_EXPIRES_AT = "sensimity_expires_at";
    private static final String REFRESH_TOKEN = "sensimity_refresh_token";

    /**
     * Constructor
     *
     * @param context Application context
     */
    public AuthStorage(Context context) {
        this.context = context;
    }

    /**
     * Get earlier saved Sensimity refresh_token
     *
     * @return refresh_token. if not found, returns a empty string
     */
    public String getRefreshTokenFromPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(API_PREFS, Context.MODE_PRIVATE);
        if (prefs.contains(REFRESH_TOKEN)) {
            return prefs.getString(REFRESH_TOKEN, "");
        }

        return "";
    }

    /**
     * Get earlier saved Sensimity access_token
     *
     * @return access_token. if not found, returns a empty string
     */
    public String getAccessTokenFromPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(API_PREFS, Context.MODE_PRIVATE);
        if (prefs.contains(API_TOKEN)) {
            return prefs.getString(API_TOKEN, null);
        }

        return null;
    }

    /**
     * Save the Sensimity refresh_token to the preferences
     *
     * @param refreshToken Token to save
     */
    public void saveRefreshToken(String refreshToken) {
        SharedPreferences prefs = context.getSharedPreferences(API_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    /**
     * Save the Sensimity access_token and expires-time to the preferences
     *
     * @param accessToken Token to save
     * @param expiresAt Seconds till the token will expire
     */
    public void saveAccessToken(String accessToken, long expiresAt) {
        SharedPreferences prefs = context.getSharedPreferences(API_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(API_EXPIRES_AT, System.currentTimeMillis() + expiresAt);
        editor.putString(API_TOKEN, accessToken).apply();
    }

    /**
     * Retrieves the expiring time from the saved preferences
     *
     * @return The time till the access_token expires in milliseconds since January 1, 1970 00:00:00.0 UTC.
     */
    public long getExpiresAtFromPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(API_PREFS, Context.MODE_PRIVATE);
        if (prefs.contains(API_TOKEN)) {
            return prefs.getLong(API_EXPIRES_AT, System.currentTimeMillis());
        }
        return System.currentTimeMillis();
    }

    /**
     * Remove the Sensimity tokens from preferences
     */
    public void clearAllTokens() {
        SharedPreferences prefs = context.getSharedPreferences(API_PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    /**
     * Check the access_token is expired and needs a new one by using the refresh_token.
     *
     * @return If a new access_token is required for a new request, return true. Otherwise return false.
     */
    public boolean shouldRefresh() {
        long expiresAt = getExpiresAtFromPrefs();
        if (expiresAt == 0) {
            SharedPreferences prefs = context.getSharedPreferences(API_PREFS, Context.MODE_PRIVATE);
            if (prefs.contains(API_EXPIRES_AT)) {
                expiresAt = prefs.getLong(API_EXPIRES_AT, System.currentTimeMillis());
            }
        }
        return expiresAt <= System.currentTimeMillis();
    }
}
