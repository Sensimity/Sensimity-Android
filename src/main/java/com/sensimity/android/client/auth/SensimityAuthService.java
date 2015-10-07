package com.sensimity.android.client.auth;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.sensimity.android.Sensimity;
import com.sensimity.android.client.helpers.SSLHelper;

import org.apache.commons.codec.binary.Base64;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

/**
 * Class to authenticate to the Sensimity API
 */
public class SensimityAuthService {

    private AuthStorage authStorage;
    private static final String TAG = "SensimityAuthService";
    private static SensimityAuthService instance = null;

    /**
     * Singleton constructor for the authenticationservice
     */
    protected SensimityAuthService() {
        // disable SSLv3 because of an error on Android 4.3 See http://stackoverflow.com/a/29946540
        try {
            SSLHelper.setupNoSSLv3();
        } catch (Exception e) {
            e.printStackTrace();
        }
        authStorage = new AuthStorage(Sensimity.getContext());
        authStorage.getAccessTokenFromPrefs();
    }

    /**
     * Get the authentication-instance
     *
     * @return A singleton instance of the SensimityAuthService
     */
    public static SensimityAuthService getInstance() {
        if (instance == null) {
            instance = new SensimityAuthService();
        }
        return instance;
    }

    /**
     * Check the possibility to authenticate to the Sensimity API
     *
     * @return True if access_token still valid, otherwise returns false
     */
    public boolean isAuthorized() {
        String accessToken = authStorage.getAccessTokenFromPrefs();
        return (accessToken != null && !shouldRefresh());
    }

    /**
     * Check the access_token is expired and needs a new one by using the refresh_token.
     *
     * @return If a new access_token is required for a new request, return true. Otherwise return false.
     */
    public boolean shouldRefresh() {
        return authStorage.shouldRefresh();
    }

    /**
     * Get earlier saved Sensimity access_token
     *
     * @return Access token if available
     */
    public String getAccessToken() {
        String accessToken = authStorage.getAccessTokenFromPrefs();
        if (accessToken != null) {
            return accessToken;
        }

        throw new IllegalStateException("no access token is available yet");
    }

    /**
     * If a new access_token is required, send a request to
     */
    public void refreshAccessTokenSynchronously() throws OAuthProblemException {
        try {
            authStorage.saveAccessToken(null, 0);

            OAuthClientRequest request;
            if (authStorage.getRefreshTokenFromPrefs().equals("")) {
                request = buildObtainTokenRequest();
            } else {
                request = buildRefreshAccessTokenRequest();
            }

            sendRequest(request);
        } catch (OAuthSystemException e) {
            Log.e(TAG, "Exception while send a request to authenticate at the Sensimity-API ", e);
        }
    }

    /**
     * Build request to obtain the access_token and refresh_token from the Sensimity-API
     *
     * @return request Created request
     * @throws OAuthSystemException Throws if the request building-process failed
     */
    protected OAuthClientRequest buildObtainTokenRequest() throws OAuthSystemException {
        OAuthClientRequest request = new OAuthClientRequest.AuthenticationRequestBuilder(Constants.OAUTH_TOKEN_URL)
                .setParameter("username", Sensimity.getUsername())
                .setParameter("password", Sensimity.getPassword())
                .setParameter("grant_type", GrantType.PASSWORD.toString())
                .buildBodyMessage();
        addAuthorizationHeader(request);

        return request;
    }

    /**
     * Build request for the Sensimity-API to refresh the access_token based on the refresh_token
     *
     * @return request Created request
     * @throws OAuthSystemException Throws if the request building-process failed
     */
    protected OAuthClientRequest buildRefreshAccessTokenRequest() throws OAuthSystemException {
        OAuthClientRequest request = new OAuthClientRequest.TokenRequestBuilder(Constants.OAUTH_TOKEN_URL)
                .setRefreshToken(authStorage.getRefreshTokenFromPrefs())
                .setGrantType(GrantType.REFRESH_TOKEN)
                .buildBodyMessage();
        addAuthorizationHeader(request);

        return request;
    }

    /**
     * Add Base64 encoded basic authorization header to the OAuth request
     *
     * @param request Earlier created authentication request
     */
    protected void addAuthorizationHeader(OAuthClientRequest request) {
        String authString = Sensimity.getClientId() + ":" + Sensimity.getClientSecret();
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        request.addHeader(OAuth.HeaderType.AUTHORIZATION, Constants.OAUTH_BASIC_HEADER_NAME + " " + authStringEnc);
    }

    /**
     * Send authentication request
     *
     * @param request Request to send to Sensimity-API
     * @throws OAuthSystemException Throws if sending of this request failed
     */
    protected void sendRequest(OAuthClientRequest request) throws OAuthSystemException, OAuthProblemException {
        try {
            OAuthClient client = new OAuthClient(new URLConnectionClient());
            OAuthJSONAccessTokenResponse response = client.accessToken(request, OAuth.HttpMethod.POST);
            saveTokens(response);
        } catch (OAuthProblemException e) {
            // Remove all tokens, retry
            if (e.getError().equals(OAuthError.TokenResponse.INVALID_REQUEST) ) {
                authStorage.clearAllTokens();
            }
            throw e;
        }
    }

    /**
     * Save refresh and access_token
     *
     * @param response The response which containing both access- and refresh_tokens
     */
    protected void saveTokens(OAuthJSONAccessTokenResponse response) {
        String refreshToken = response.getRefreshToken();
        long expiresAt = response.getExpiresIn() * DateUtils.SECOND_IN_MILLIS;
        authStorage.saveAccessToken(response.getAccessToken(), expiresAt);

        if (!TextUtils.isEmpty(refreshToken)) {
            authStorage.saveRefreshToken(refreshToken);
        }
    }
}
