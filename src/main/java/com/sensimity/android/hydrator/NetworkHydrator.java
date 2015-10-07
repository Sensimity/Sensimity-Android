package com.sensimity.android.hydrator;

import android.support.annotation.NonNull;

import com.sensimity.android.model.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NetworkHydrator {

    /**
     * Hydrate an list of networks from the SensimityAPI
     *
     * @param response A responsebody
     * @return Arraylist containing hydrated networks
     */
    public ArrayList<com.sensimity.android.model.Network> hydrateList(String response) {
        ArrayList<com.sensimity.android.model.Network> networks = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray networkArray = jsonResponse.getJSONObject("_embedded").getJSONArray("network");

            for (int i = 0; i < networkArray.length(); i++) {
                JSONObject jsonNetwork = networkArray.getJSONObject(i);
                networks.add(hydrateHetwork(jsonNetwork));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return networks;
    }

    @NonNull
    protected Network hydrateHetwork(JSONObject item) throws JSONException {
        Network network = new Network();
        network.setNetworkId(item.getInt("network_id"));
        network.setAccountId(item.getInt("account_id"));
        network.setName(item.getString("name"));

        return network;
    }
}
