package com.sensimity.android.client.auth;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;

import java.io.IOException;

/**
 * RequestManager to handle the requests to the SensimityAPI
 */
public class RequestManager implements Interceptor {

    private static RequestManager instance = null;
    private final OkHttpClient httpClient;
    private final SensimityAuthService sensimityAuthService;

    /**
     * Constructor for singleton
     */
    protected RequestManager() {
        httpClient = new OkHttpClient();
        httpClient.interceptors().add(this);
        sensimityAuthService = SensimityAuthService.getInstance();
    }

    /**
     * Get a Singleton instance of this RequestManager class
     *
     * @return A instance of RequestManager
     */
    public static RequestManager getInstance() {
        if (instance == null) {
            instance = new RequestManager();
        }
        return instance;
    }

    /**
     * Httpclient created during the instance of the RequestManager
     *
     * @return OkHttpClient to send http-requests
     */
    public OkHttpClient getClient() {
        return httpClient;
    }

    /**
     * Intercept every request to add a access_token
     *
     * @param chain Chain containing requests and responses
     * @return Request with bearertoken
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        // If the current access token is expired, be proactive in updating it
        if (sensimityAuthService.shouldRefresh()) {
            try {
                sensimityAuthService.refreshAccessTokenSynchronously();
            } catch (OAuthProblemException e) {
                // Retry because the refresh token has expired properly
                try {
                    sensimityAuthService.refreshAccessTokenSynchronously();
                } catch (OAuthProblemException e1) {
                    e1.printStackTrace();
                }
            }
        }

        // Check the user is authorized, then add the accesstoken to the request
        if (sensimityAuthService.isAuthorized()) {
            Request request = addAccessTokenToRequest(chain.request());
            return chain.proceed(request);
        }

        throw new IllegalStateException("No bearertoken added to the request");
    }

    /**
     * Add the bearertoken to a request, required for authenticated requests
     *
     * @param request To add the access_token
     * @return
     */
    protected Request addAccessTokenToRequest(Request request) {
        String bearerString = OAuth.OAUTH_HEADER_NAME + " " + sensimityAuthService.getAccessToken();

        request = request.newBuilder()
                .header("Accept", Constants.ACCEPT_HEADER)
                .header(OAuth.HeaderType.AUTHORIZATION, bearerString)
                .build();
        return request;
    }
}