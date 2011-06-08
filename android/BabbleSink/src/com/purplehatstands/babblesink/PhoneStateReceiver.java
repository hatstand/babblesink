package com.purplehatstands.babblesink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(this.getClass().getCanonicalName(), "Phone state changed!");
    String newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
    Log.d(this.getClass().getCanonicalName(), "Phone state changed to:" + newState);
    if (newState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
      // Phone has started ringing.
      String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
      Log.d(this.getClass().getCanonicalName(), "Phone is ringing! from:" + number);
    }
  }

}
