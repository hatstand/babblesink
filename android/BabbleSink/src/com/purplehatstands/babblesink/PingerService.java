package com.purplehatstands.babblesink;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.http.cookie.Cookie;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

public class PingerService extends IntentService {
  private static final String TAG = PingerService.class.getCanonicalName();
  private static PowerManager.WakeLock wakeLock;
  
  
  public PingerService() {
    super("PingerService");
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
    final AccountManager accountManager = (AccountManager)getSystemService(ACCOUNT_SERVICE);
    final Account googleAccount = accountManager.getAccountsByType("com.google")[0];
    final String state = intent.getStringExtra("state");
    final String number = intent.getStringExtra("number");
    try {
      String authToken = accountManager.blockingGetAuthToken(googleAccount, "ah", true);
      Cookie cookie = new GetCookieTask().execute(authToken).get();
      if (cookie == null) {
        // Retry once; auth token may have expired.
        if (retries > 0) {
          return;
        }
        accountManager.invalidateAuthToken("com.google", authToken);
        sendUpdate(intent, ++retries);
      }
      
      if (cookie != null) {
        Log.d(TAG, "Got cookie:" + cookie.toString());
        new UpdateTask().execute(cookie, state, number);
      } else {
        Log.d(TAG, "Failed to get cookie");
      }
    } catch (OperationCanceledException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AuthenticatorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
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
