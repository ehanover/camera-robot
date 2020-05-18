package com.example.camera_robot;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

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
    int sendTime = 1500; // How often the post-serial-update process happens, in ms

    UsbManager manager;
    UsbDevice device = null;
    UsbSerialDevice serial;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            usbPermissionAccepted();
                        }
                    }
                    else {
                        Log.d(TAG, "Permission denied for device=" + device);
                    }
                }
            }
        }
    };

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
        usbPermissionAsk();

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
        img.compressToJpeg(new Rect(0, 0, width, height), 30, out); // TODO adjust
        byte[] out_bytes = out.toByteArray();

        Log.d(TAG, "Sending image with byte length=" + out_bytes.length);
        volleyPost(out_bytes);

        lastSentMillis = System.currentTimeMillis();
    }

    public void streamClose(MethodCall methodCall) {
        // queue.cancelAll(_);
        // stopService(new Intent(this, GetService.class));
        usbSendData("");
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
                usbSendData(resultResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley POST error"); // ,code: " + networkResponse.statusCode);
                error.printStackTrace();
                usbSendData("");
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

    private void usbPermissionAsk() {
        Log.d(TAG, "usbSetupPermission called");
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        for(UsbDevice d : deviceList.values()) {
            if(d.getManufacturerName().contains("rduino")) {
                device = d;
                break;
            }
        }
        if(device == null) {
            Log.e(TAG, "setupUsb: could not find a device with correct product name");
            return;
        }
        //private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
        manager.requestPermission(device, mPermissionIntent);
    }

    private void usbPermissionAccepted() {
        Log.d(TAG, "usbSetupConnection called");
        // Assuming permission has been granted because this function gets called by broadcast receiver
        // Arduino is 8-N-1 serial communication
        UsbDeviceConnection usbConnection = manager.openDevice(device);
        // https://github.com/felHR85/UsbSerial
        // Consider using https://github.com/OmarAflak/Arduino-Library
        serial = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);
        serial.open();
        serial.setBaudRate(9600);
        serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
        serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
        serial.setParity(UsbSerialInterface.PARITY_EVEN);
        serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
    }

    private void usbSendData(String data) {
        if(serial == null) { // USB permission hasn't been granted (or the Arduino isn't connected to the phone?)
            return;
        }
        Log.d(TAG, "usbSendData sending data=" + data);
        // data looks like 0100-030 sLLLsRRR

        if(data.equals("")) { // Stop motors
            serial.write( "00000000|".getBytes() );
        } else { // Move motors
            serial.write( (data + "|").getBytes() );
        }
    }


}
