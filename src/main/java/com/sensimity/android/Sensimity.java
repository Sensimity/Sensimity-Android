package com.sensimity.android;

import android.content.Context;

public class Sensimity {
    protected static Sensimity instance = null;

    protected static Context context = null;
    protected static String username = null;
    protected static String password = null;
    protected static String clientId = null;
    protected static String clientSecret = null;
    protected static String instanceRef = null;

    /**
     * Initialize the Sensimity library with credentials.
     *
     * @param context      Application context
     * @param username     Sensimity username
     * @param password     Sensimity password
     * @param clientId     Sensimity oAuth client id
     * @param clientSecret Sensimity oAuth client secret
     * @param instanceRef  Instance ref used for sending the scan results
     * @return Sensimity instance
     */
    public static void initialize(Context context, String username, String password, String clientId, String clientSecret, String instanceRef) {
        if (instance == null) {
            instance = new Sensimity();
        }

        Sensimity.context = context;
        Sensimity.username = username;
        Sensimity.password = password;
        Sensimity.clientId = clientId;
        Sensimity.clientSecret = clientSecret;
        Sensimity.instanceRef = instanceRef;
    }

    /**
     * Getter
     *
     * @return context
     */
    public static Context getContext() {
        return context;
    }

    /**
     * Getter
     *
     * @return username
     */
    public static String getUsername() {
        return username;
    }

    /**
     * Getter for password
     *
     * @return password
     */
    public static String getPassword() {
        return password;
    }

    /**
     * Getter for the sensimity client id
     *
     * @return client id
     */
    public static String getClientId() {
        return clientId;
    }

    /**
     * Getter for the client secret key
     *
     * @return secret
     */
    public static String getClientSecret() {
        return clientSecret;
    }

    /**
     * Instance ref used for sending the scan results
     *
     * @return instanceref
     */
    public static String getInstanceRef() {
        return instanceRef;
    }
}