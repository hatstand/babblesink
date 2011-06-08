package com.purplehatstands.babblesink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public class UpdateTask extends AsyncTask<Object, Integer, Boolean> {

  AndroidHttpClient httpClient;
  HttpContext httpContext;
  CookieStore cookies;
  
  public UpdateTask() {
    cookies = new BasicCookieStore();
    httpContext = new BasicHttpContext();
    httpContext.setAttribute(ClientContext.COOKIE_STORE, cookies);
    httpClient = AndroidHttpClient.newInstance("Babblesink");
  }
  
  
  @Override
  protected Boolean doInBackground(Object... params) {
    Cookie cookie = (Cookie)params[0];
    String state = (String)params[1];
    String number = (String)params[2];
    cookies.addCookie(cookie);
    HttpPost post = new HttpPost("https://ovraiment.appspot.com/et/phone");
    List<NameValuePair> postParams = new ArrayList<NameValuePair>();
    postParams.add(new BasicNameValuePair("state", state));
    postParams.add(new BasicNameValuePair("number", number));
    try {
      HttpResponse response = httpClient.execute(post, httpContext);
      if (response.getStatusLine().getStatusCode() == 200) {
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return false;
  }
  
  @Override
  protected void onPostExecute(Boolean result) {
    httpClient.close();
  }
}
