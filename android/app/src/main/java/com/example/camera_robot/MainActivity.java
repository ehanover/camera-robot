package com.example.camera_robot;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
    static final String TAG = "ASDF";
    static final String CHANNEL = "com.example.camera_stream_test3";

    // boolean sentOnce;
    RequestQueue queue;
    GetService getService;
    long lastSentMillis;
    String address;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(
                (methodCall, result) -> {
                    if (methodCall.method.equals("streamStart")) {
                        Log.d(TAG, "Init stream...");
                        streamInit(methodCall);
                    } else if (methodCall.method.equals("streamImage")) {
                        streamImage(methodCall);
                    } else if(methodCall.method.equals("streamClose")) {
                        Log.d(TAG, "Trying to close stream...");
                        streamClose(methodCall);
                    }
                }
        );
    }

    public void streamInit(MethodCall methodCall) {
        String a = methodCall.argument("address"); // "192.168.0.225";
        // int p = methodCall.argument("port");
        address = a; // + ":" + p;
        queue = Volley.newRequestQueue(this);

        getService = new GetService("GetServiceIntent", queue, address);
        lastSentMillis = -1;
        // sentOnce = false;
        Log.d(TAG, "Done with init stream, address=" + address);
    }

    public void streamImage(MethodCall methodCall) {
        if(System.currentTimeMillis() < lastSentMillis + 1200) {
            // Log.d(TAG, "Skipping because of time delay");
            return;
        }
//        if(sentOnce)
//            return;
//        sentOnce = true;

        int width = 320; // These should probably be methodCall arguments
        int height = 240;

        ArrayList<byte[]> bytes = methodCall.argument("bytes");
        YuvImage img = new YuvImage(bytes.get(0), ImageFormat.NV21, width, height, null);
        // String s = new String(bytes.get(0), StandardCharsets.UTF_8); // Plain bytes straight to string

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        img.compressToJpeg(new Rect(0, 0, width, height), 20, out);
        byte[] out_bytes = out.toByteArray();
        // new SavePhotoTask().execute(out_bytes); // Save the photo
        // String s = new String(out_bytes, StandardCharsets.UTF_8); // String from compressed image

        Log.d(TAG, "Sending byte message with length=" + out_bytes.length);
        volleyPost(out_bytes);

        lastSentMillis = System.currentTimeMillis();
    }

    public void streamClose(MethodCall methodCall) {
        // queue.cancelAll(_);
        getService.stopService(new Intent(this, GetService.class));
        queue.cancelAll(request -> true);
        queue.stop();
    }


    private void volleyPost(byte[] bytes) {
        // https://developer.android.com/training/volley/simple#java
        // https://stackoverflow.com/questions/32240177/working-post-multipart-request-with-volley-and-without-httpentity
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, address, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                 String resultResponse = new String(response.data);
                Log.d(TAG, "Volley POST response: " + resultResponse);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley POST error"); // ,code: " + networkResponse.statusCode);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fake_data_key", "fake_data_value");
                // TODO add data like location, speed, status, battery, time?
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path, for now just get bitmap data from ImageView
                params.put("file", new DataPart("file", bytes, "image/jpeg"));
                // params.put("file", new DataPart("file", new byte[]{65, 66, 127, 67}, "image/jpeg"));
                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                1000, // TODO experiment with timeout values
                0, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // May cause frames to be sent out of order, so don't retry
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        queue.add(multipartRequest);
    }



}
