package com.structurecode.alto.Helpers;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.structurecode.alto.AuthActivity;
import com.structurecode.alto.Models.Song;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Guy on 4/24/2017.
 */

public class Utils {
    public static final String POST_REQUEST="POST";
    public static final String PUT_REQUEST="PUT";
    public static final String GET_REQUEST="GET";
    public static final String DELETE_REQUEST="DELETE";

    public static final String COLLECTION_USERS="users";
    public static final String COLLECTION_SETTINGS="settings";
    public static final String COLLECTION_LIBRARY="library";
    public static final String COLLECTION_RECORD="record";

    public static final String DEVICE_CHECK="com.structurecode.alto.device.check";

    public static FirebaseAuth mAuth=FirebaseAuth.getInstance();
    public static FirebaseFirestore db= FirebaseFirestore.getInstance();
    public static FirebaseUser user = mAuth.getCurrentUser();

    public static String music_url = "https://storage.googleapis.com/alto-a7134.appspot.com/";

    public static void show_mini_player(boolean show, Context context, CoordinatorLayout coordinatorLayout, LinearLayout mini_player){
        final float scale = context.getResources().getDisplayMetrics().density;
        if (show){
            CoordinatorLayout.MarginLayoutParams params = (CoordinatorLayout.MarginLayoutParams) coordinatorLayout.getLayoutParams();
            int margin=(int) (50*scale);
            params.setMargins(0,0,0,margin);
            coordinatorLayout.setLayoutParams(params);
            mini_player.setVisibility(View.VISIBLE);
        }else{
            mini_player.setVisibility(View.GONE);
            CoordinatorLayout.MarginLayoutParams params = (CoordinatorLayout.MarginLayoutParams) coordinatorLayout.getLayoutParams();
            params.setMargins(0,0,0,0);
            coordinatorLayout.setLayoutParams(params);
        }
    }

    public static void device_check_broadcast(Context context, BroadcastReceiver broadcastReceiver){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DEVICE_CHECK);
        broadcastReceiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Intent i=new Intent(context, AuthActivity.class);
                context.startActivity(i);
                ((Activity)context).finish();
            }
        };

        context.registerReceiver(broadcastReceiver,intentFilter);
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public  static String POSTRequest(String requestURL,String JSONData) throws IOException{
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String JsonResponse = "";

        try {
            URL url = new URL(requestURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");


            Writer writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            writer.write(JSONData);
            writer.flush();
            writer.close();
            InputStream inputStream = conn.getInputStream();
            /*int statusCode = conn.getResponseCode();
            Log.d("ERROR", "FFFF"+statusCode);*/

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = reader.readLine()) != null)
                buffer.append(inputLine + "\n");
            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            JsonResponse = buffer.toString();
            return JsonResponse;
        }finally {
            conn.disconnect();
        }
    }

    public  static String put_post_request(String requestURL,String JSONData,String request,String token) throws IOException{
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String JsonResponse = "";

        try {
            URL url = new URL(requestURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);

            conn.setRequestMethod(request);
            conn.setRequestProperty("Authorization", "Token "+token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            Writer writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            writer.write(JSONData);
            writer.flush();
            writer.close();
            InputStream inputStream = conn.getInputStream();

            /*int statusCode = conn.getResponseCode();
            Log.d("ERROR", "FFFF"+statusCode);*/

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = reader.readLine()) != null)
                buffer.append(inputLine + "\n");
            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            JsonResponse = buffer.toString();
            return JsonResponse;
        }finally {
            conn.disconnect();
        }
    }

    public  static String get_delete_request(String requestURL,String request,String token) throws IOException{
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String JsonResponse = "";

        try {
            URL url = new URL(requestURL);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod(request);
            conn.setRequestProperty("Authorization", "Token "+token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            InputStream inputStream = conn.getInputStream();

            int statusCode = conn.getResponseCode();
            Log.d("ERROR", "FFFF"+statusCode);

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = reader.readLine()) != null)
                buffer.append(inputLine + "\n");
            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            JsonResponse = buffer.toString();
            return JsonResponse;
        }finally {
            conn.disconnect();
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String get_device_id(Context context) {

        String pseudoId = "35" +
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10;

        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String btId = "";

        if (bluetoothAdapter != null) {
            btId = bluetoothAdapter.getAddress();
        }

        String longId = pseudoId + androidId + btId;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(longId.getBytes(), 0, longId.length());

            // get md5 bytes
            byte md5Bytes[] = messageDigest.digest();

            // creating a hex string
            String identifier = "";

            for (byte md5Byte : md5Bytes) {
                int b = (0xFF & md5Byte);

                // if it is a single digit, make sure it have 0 in front (proper padding)
                if (b <= 0xF) {
                    identifier += "0";
                }

                // add number to string
                identifier += Integer.toHexString(b);
            }

            // hex string to uppercase
            identifier = identifier.toUpperCase();
            return identifier;
        } catch (Exception e) {
            Log.e("TAG", e.toString());
        }
        return "";
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String device_name_id(Context context){
        return getDeviceName()+"_"+get_device_id(context);
    }


}
