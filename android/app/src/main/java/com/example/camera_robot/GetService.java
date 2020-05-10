package com.example.camera_robot;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import io.flutter.Log;

public class GetService extends IntentService {
    static final String TAG = "ASDF";

    RequestQueue queue;
    String address;

    public GetService() {
        super("GetServiceIntent_EmptyConstructor");
        Log.e(TAG, "GetService default constructor called");
    }

    public GetService(String name, RequestQueue queue, String address) {
        super(name);
        this.queue = queue;
        this.address = address;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try {
            while(true) {
                volleyGet();
                Thread.sleep(1400);
            }
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }

    }

    private void volleyGet() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, address,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Volley GET got response: " + response);
                        serialSend(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error in volley GET: " + error);
                    }
                }
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void serialSend(String data) {
        // TODO implement
    }
}
