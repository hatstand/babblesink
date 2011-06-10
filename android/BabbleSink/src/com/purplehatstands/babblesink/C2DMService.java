package com.purplehatstands.babblesink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

public class C2DMService extends IntentService {
  private static final String TAG = "babblesink.C2DMService";
  
  private static PowerManager.WakeLock wake_lock_;
  private static final String WAKELOCK_KEY = "com.purplehatstands.babblesink.C2DMService";
  
  private static final String REGISTER_PATH = "https://ovraiment.appspot.com/c2dm/register";
  
  private AppengineClient client;

  public C2DMService() {
    super("C2DMService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    client = new AppengineClient(this);
    Log.d(TAG, "onHandleIntent");
    try {
      if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
        handleRegistration(intent);
      } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
        handleMessage(intent);
      }
    } finally {
      wake_lock_.release();
    }
  }
  
  // C2DM
  private void handleRegistration(Intent intent) {
    Log.d(TAG, "HandleRegistration");
    String registration = intent.getStringExtra("registration_id"); 
    if (intent.getStringExtra("error") != null) {
        // Registration failed, should try again later.
    } else if (intent.getStringExtra("unregistered") != null) {
        // Unregistration done, new messages from the authorized sender will be rejected
    } else if (registration != null) {
       // Send the registration ID to the 3rd party site that is sending the messages.
       // This should be done in a separate thread.
       // When done, remember that all registration is done.
      Log.d(TAG, "C2DM Registration: " + registration);

      SharedPreferences prefs = getSharedPreferences("c2dm", Context.MODE_PRIVATE);
      Editor editor = prefs.edit();
      editor.putString("c2dm_reg", registration);
      editor.commit();
      if (!prefs.getBoolean("registered", false)) {
        // Register with our server if we haven't yet.
        registerDeviceWithServer(registration);
      }
    }
  }
  
  private void registerDeviceWithServer(String registrationId) {
    Log.d(TAG, "RegisterDeviceWithServer");
    
    HttpPost post = new HttpPost(REGISTER_PATH);
    List<NameValuePair> postData = new ArrayList<NameValuePair>();
    postData.add(new BasicNameValuePair("registration_id", registrationId));
    postData.add(new BasicNameValuePair("brand", Build.BRAND));
    postData.add(new BasicNameValuePair("manufacturer", Build.MANUFACTURER));
    postData.add(new BasicNameValuePair("device", Build.DEVICE));
    postData.add(new BasicNameValuePair("model", Build.MODEL));
   
    try {
      post.setEntity(new UrlEncodedFormEntity(postData));
      client.sendRequest(post);
      
      SharedPreferences prefs = getSharedPreferences("c2dm", Context.MODE_PRIVATE);
      Editor editor = prefs.edit();
      editor.putBoolean("registered", true);
      editor.commit();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  // C2DM
  private void handleMessage(Intent intent) {
    String accountName = intent.getExtras().getString("account");
    String message = intent.getExtras().getString("message"); 
    Log.d(TAG, "Message received from: " + accountName);
    Log.d(TAG, "Message: " + message);
  }
  
  public static void runIntentInService(Context context, Intent intent) {
    if (wake_lock_ == null) {
      PowerManager power_manager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
      wake_lock_ = power_manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
    }
    wake_lock_.acquire();
    
    intent.setClass(context, C2DMService.class);
    context.startService(intent);
  }
}
