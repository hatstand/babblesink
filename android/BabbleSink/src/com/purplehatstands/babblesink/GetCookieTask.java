package com.purplehatstands.babblesink;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class GetCookieTask extends AsyncTask<String, Integer, Cookie> {
  AndroidHttpClient httpClient;
  HttpContext httpContext;
  CookieStore cookies;
  
  public GetCookieTask() {
    httpClient = AndroidHttpClient.newInstance("Babblesink");
    httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    cookies = new BasicCookieStore();
    httpContext = new BasicHttpContext();
    httpContext.setAttribute(ClientContext.COOKIE_STORE, cookies);
  }
  
  
  @Override
  protected Cookie doInBackground(String... params) {
    String token = params[0];
    String url = "https://ovraiment.appspot.com/_ah/login?continue=http://localhost/&auth=" + token;
    HttpGet get = new HttpGet(url);
    Log.d("Babblesink", url);
    try {
      HttpResponse response = httpClient.execute(get, httpContext);
      if (response.getStatusLine().getStatusCode() != 302) {
        Log.d("Babblesink", "Unexpected status code:" + response.getStatusLine().toString());
        return null;
      }
      
      for (Cookie cookie : cookies.getCookies()) {
        Log.d("Babblesink", "Cookie:" + cookie.getName());
        if (cookie.getName().equals("SACSID")) {  // Secure Appengine cookie.
          return cookie;
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

}
