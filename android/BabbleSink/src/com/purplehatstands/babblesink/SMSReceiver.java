package com.purplehatstands.babblesink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      Object[] pdus = (Object[])bundle.get("pdus");
      for (int i = 0; i < pdus.length; ++i) {
        SmsMessage message = SmsMessage.createFromPdu((byte[])pdus[i]);
        Log.d(this.getClass().getCanonicalName(), "Received SMS:" + message.getMessageBody());
      }
    }
  }

}
