package com.purplehatstands.babblesink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class PingerService extends IntentService {
  private static final String TAG = PingerService.class.getCanonicalName();
  private static PowerManager.WakeLock wakeLock;
  
  private AppengineClient client;
  
  
  public PingerService() {
    super("PingerService");
    client = new AppengineClient(this);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      sendUpdate(intent, 0);
    } finally {
      wakeLock.release();
    }
  }
  
  private void sendUpdate(Intent intent, int retries) {
    final String state = intent.getStringExtra("state");
    final String number = intent.getStringExtra("number");
    HttpPost post = new HttpPost("https://ovraiment.appspot.com/et/phone");
    List<NameValuePair> postParams = new ArrayList<NameValuePair>();
    postParams.add(new BasicNameValuePair("state", state));
    postParams.add(new BasicNameValuePair("number", number));
    try {
      client.sendRequest(post);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void runIntentInService(Context context, Intent intent) {
    if (wakeLock == null) {
      final PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PingerService.class.getCanonicalName());
    }
    wakeLock.acquire();
    intent.setClass(context, PingerService.class);
    context.startService(intent);
  }
}
