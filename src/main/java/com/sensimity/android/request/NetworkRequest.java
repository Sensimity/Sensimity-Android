package com.sensimity.android.request;

import android.util.Log;

import com.sensimity.android.client.auth.RequestManager;
import com.sensimity.android.hydrator.NetworkHydrator;
import com.sensimity.android.model.Network;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;

public class NetworkRequest {

    public interface SuccessResponseListener {
        void handle(ArrayList<Network> networks);
    }

    /**
     * Get all networks of current account
     *
     * @param listener Handle success response
     */
    public void getAll(final SuccessResponseListener listener) {
        String url = com.sensimity.android.client.auth.Constants.SENSIMITY_URL + Constants.NETWORKS_URL;
        Request request = buildGetRequest(url);
        Callback callback = new Callback() {
            @Override
            public void onFailure (Request request, IOException e){
                Log.d("NETWORKREQUEST", "Getall networks from Sensimity failed ", e);
            }

            @Override
            public void onResponse (Response response) throws IOException {
                handleResponse(listener, response);
            }
        };

        sendRequest(request, callback);
    }

    protected void handleResponse(SuccessResponseListener listener, Response response) throws IOException {
        if (response.isSuccessful()) {
                NetworkHydrator hydrator = new NetworkHydrator();
                ArrayList<Network> networks = hydrator.hydrateList(response.body().string());
                listener.handle(networks);
        } else {
            Log.d("NETWORKREQUEST", "Getall networks from Sensimity failed " + response.body().string());
        }
    }

    protected Request buildGetRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        return request;
    }

    protected void sendRequest(Request request, Callback responseCallback) {
        RequestManager.getInstance().getClient().newCall(request).enqueue(responseCallback);
    }
}
