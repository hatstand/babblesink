package com.purplehatstands.babblesink;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class AppengineClient {
  private String authToken;
  private AccountManager accountManager;
  private Account account;
  
  private AndroidHttpClient httpClient = AndroidHttpClient.newInstance("babblesink");
  private CookieStore cookieStore = new BasicCookieStore();
  private HttpContext httpContext = new BasicHttpContext();
  
  
  public AppengineClient(Context context) {
    accountManager = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);
    account = accountManager.getAccountsByType("com.google")[0];

    httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
  }
  
  @Override
  protected void finalize() {
    httpClient.close();
  }
  
  public HttpEntity sendRequest(HttpUriRequest request) throws IOException {
    if (getCookie() == null) {
      if (!fetchCookie()) {
        // One retry for the case where the ClientLogin token has expired.
        accountManager.invalidateAuthToken("com.google", authToken);
        authToken = null;
        if (!fetchCookie()) {
          throw new IOException("Failed to get appengine cookie");
        }
      }
    }
    
    HttpResponse response = httpClient.execute(request, httpContext);
    if (response.getStatusLine().getStatusCode() != 200) {
      Log.d("Babblesink", "Unexpected status code:" + response.getStatusLine().toString());
      throw new IOException("Failed fetching from appengine:" + response.getStatusLine().getReasonPhrase());
    }
    
    return response.getEntity();
  }
  
  private Cookie getCookie() {
    for (Cookie cookie : cookieStore.getCookies()) {
      if (cookie.getName() == "SACSID") {
        Date now = Calendar.getInstance().getTime();
        Date soon = new Date(now.getTime() + 1000 * 30);  // 30s in the future.
        if (!cookie.isExpired(soon)) {
          return cookie;
        }
      }
    }
    return null;
  }
  
  private boolean fetchCookie() throws IOException {
    if (authToken == null) {
      authToken = fetchAuthToken();
    }
    httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    String url = "https://ovraiment.appspot.com/_ah/login?continue=http://localhost/&auth=" + authToken;
    HttpGet get = new HttpGet(url);
    HttpResponse response;
    response = httpClient.execute(get, httpContext);
    if (response.getStatusLine().getStatusCode() != 302) {
      Log.d("Babblesink", "Unexpected status code:" + response.getStatusLine().toString());
      return false;
    }
    
    for (Cookie cookie : cookieStore.getCookies()) {
      if (cookie.getName().equals("SACSID")) {  // Secure Appengine cookie.
        Log.d("Babblesink", "Successfully got appengine cookie");
        return true;
      }
    }
    return false;
  }
  
  private String fetchAuthToken() {
    try {
      return accountManager.blockingGetAuthToken(account, "ah", true);
    } catch (OperationCanceledException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AuthenticatorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
}
