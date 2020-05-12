package com.example.camera_robot;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.felhr.usbserial.UsbSerialDevice;

import java.util.HashMap;
import java.util.Iterator;

import io.flutter.Log;

public class GetService extends IntentService {
    static final String TAG = "ASDF";

    RequestQueue queue;
    String address;
    // Context context;

    public GetService() {
        super("GetServiceIntent_EmptyConstructor");
        Log.e(TAG, "GetService default constructor called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        address = intent.getStringExtra("address");
        // queue = null; // TODO fix queue and serial instantiation - use more intent extras?
        return Service.START_STICKY;
    }

    /* public GetService(String name, RequestQueue queue, String address) {
        super(name);
        Log.d(TAG, "GetService non-default constructor called");
        this.queue = queue;
        this.address = address;

        // Arduino is 8-N-1 serial communication
//        UsbDevice device = new UsbDevice();
//        UsbDeviceConnection usbConnection;
//        UsbSerialDevice serial = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);
    } */

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
        Log.d(TAG, "Starting volleyGet");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, address,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Volley GET got response: " + response);
                        // serialSend(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error in volley GET: " + error);
                    }
                }
        );

        queue.add(stringRequest); // Add the request to the RequestQueue.
    }

//    private void serialSend(String data) {
//        Log.d(TAG, "Starting serialSend");
//        // TODO implement
//    }
}
