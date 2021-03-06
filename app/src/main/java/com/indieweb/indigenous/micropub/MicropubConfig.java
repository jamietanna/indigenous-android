package com.indieweb.indigenous.micropub;

import android.accounts.AccountManager;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MicropubConfig {

    private final Context context;
    private final User user;

    public MicropubConfig(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    /**
     * Refresh config.
     */
    public void refresh() {

        String MicropubEndpoint = user.getMicropubEndpoint();

        // Some endpoints already contain GET params.
        if (MicropubEndpoint.contains("?")) {
            MicropubEndpoint += "&q=config";
        }
        else {
            MicropubEndpoint += "?q=config";
        }

        StringRequest getRequest = new StringRequest(Request.Method.GET, MicropubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Syndication targets.
                        try {
                            JSONObject micropubResponse = new JSONObject(response);
                            JSONArray itemList = micropubResponse.getJSONArray("syndicate-to");
                            if (itemList.length() > 0) {
                                AccountManager am = AccountManager.get(context);
                                am.setUserData(user.getAccount(), "syndication_targets", itemList.toString());
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(context, "Error getting syndication targets: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        // Media endpoint.
                        try {
                            JSONObject micropubResponse = new JSONObject(response);
                            if (micropubResponse.has("media-endpoint")) {
                                String micropubMediaEndpoint = micropubResponse.getString("media-endpoint");
                                if (micropubMediaEndpoint.length() > 0) {
                                    AccountManager am = AccountManager.get(context);
                                    am.setUserData(user.getAccount(), "micropub_media_endpoint", micropubMediaEndpoint);
                                }
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(context, "Error getting media endpoint: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        // Post types.
                        try {
                            JSONObject micropubResponse = new JSONObject(response);
                            if (micropubResponse.has("post-types")) {
                                JSONArray itemList = micropubResponse.getJSONArray("post-types");
                                if (itemList.length() > 0) {
                                    AccountManager am = AccountManager.get(context);
                                    am.setUserData(user.getAccount(), "post_types", itemList.toString());
                                }
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(context, "Error getting post-types: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        Toast.makeText(context, "Micropub configuration updated", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                                int code = networkResponse.statusCode;
                                String result = new String(networkResponse.data);
                                Toast.makeText(context, "Error getting configuration. Status code: " + code + "; message: " + result, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(context, "Error getting configuration: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(context, "Error getting configuration: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(getRequest);

    }

}
