package com.purplehatstands.babblesink;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class MainActivity extends Activity {
  @Override
  protected void onStart() {
    super.onStart();
    SharedPreferences prefs = getSharedPreferences("c2dm", Context.MODE_PRIVATE);
    if (!prefs.contains("registration_id")) {
      Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
      registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
      registrationIntent.putExtra("sender", "c2dm@clementine-player.org");
      startService(registrationIntent);
      Log.d("Babblesink", "Sent C2DM registration intent");
    }
  }
}
