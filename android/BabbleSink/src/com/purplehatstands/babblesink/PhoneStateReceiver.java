package com.purplehatstands.babblesink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    String newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
    if (newState == TelephonyManager.EXTRA_STATE_RINGING) {
      // Phone has started ringing.
      Log.d(this.getClass().getCanonicalName(), "Phone is ringing!");
    }
  }

}
