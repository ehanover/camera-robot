package com.example.camera_robot;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
    static final String CHANNEL = "com.example.camera_robot";

    // boolean sentOnce;
    RequestQueue queue;
    // GetService getService;
    long lastSentMillis;
    String address;
    int sendTime = 2000; // How often a post/get happens, in ms

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(
                (methodCall, result) -> { // Are the result.success(null) lines useful?
                    if (methodCall.method.equals("streamStart")) {
                        Log.d(TAG, "Init stream...");
                        streamInit(methodCall);
                        result.success(null);
                    } else if (methodCall.method.equals("streamImage")) {
                        streamImage(methodCall);
                        result.success(null);
                    } else if(methodCall.method.equals("streamClose")) {
                        Log.d(TAG, "Trying to close stream...");
                        streamClose(methodCall);
                        result.success(null);
                    } else if(methodCall.method.equals("myLog")) {
                        Log.d(TAG, "Log methodCall...");
                        result.success(getMyLog());
                    } else {
                        result.notImplemented();
                    }
                }
        );
    }

    public void streamInit(MethodCall methodCall) {
        try {
            sendTime = methodCall.argument("sendTime");
            address = methodCall.argument("address");
        } catch (NullPointerException e) {
            Log.e(TAG, "streamInit methodCall arguments are missing values. ");
            e.printStackTrace();
        }
        queue = Volley.newRequestQueue(this);

        // getService = new GetService("GetServiceIntent", queue, address);
        // startService(new Intent(this, GetService.class));

        lastSentMillis = -1;
        Log.d(TAG, "Done with streamInit, address=" + address);
    }

    public void streamImage(MethodCall methodCall) {
        if(System.currentTimeMillis() < lastSentMillis + sendTime) {
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

        Log.d(TAG, "Sending byte message with length=" + out_bytes.length);
        volleyPost(out_bytes);

        lastSentMillis = System.currentTimeMillis();
    }

    public void streamClose(MethodCall methodCall) {
        // queue.cancelAll(_);
        // stopService(new Intent(this, GetService.class));
        sendSerialData("");
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
                sendSerialData(resultResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley POST error"); // ,code: " + networkResponse.statusCode);
                error.printStackTrace();
                sendSerialData("");
            }
        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("fake_data_key", "fake_data_value");
//                // TODO return data like location, speed, status, battery, time?
//                return params;
//            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart("file", bytes, "image/jpeg"));
                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                sendTime - 100, // TODO experiment with timeout and retry values
                0, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // May cause frames to be sent out of order, so don't retry
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        queue.add(multipartRequest);
    }

    private String getMyLog() {
        String s = "USB DATA BELOW:\n";
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            s += device.toString() + "\n";
            s += "some props: " + device.getDeviceName() + ", " + device.getProductName() + ", " + device.getManufacturerName() + "\n\n";
        }
        return s;
    }

    private void sendSerialData(String data) {
        if(data.equals("")) {
            // Stop motors
        } else {
            // Move motors
        }
    }


}
