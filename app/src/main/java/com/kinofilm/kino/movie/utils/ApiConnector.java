package com.kinofilm.kino.movie.utils;

import android.util.Log;

import com.kinofilm.kino.movie.Config;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class ApiConnector {

    public JSONArray GetCustomerDetails(String user_id) {

        HttpEntity httpEntity = null;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(Config.ADMIN_PANEL_URL + "/api/get_user/?user_id=" + user_id);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            httpEntity = httpResponse.getEntity();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert HttpEntity into JSON Array
        JSONArray jsonArray = null;

        if (httpEntity != null) {
            try {
                String entityResponse = EntityUtils.toString(httpEntity);
                Log.e("Entity Response  : ", entityResponse);
                jsonArray = new JSONArray(entityResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jsonArray;
    }

    public Boolean uploadImageToserver(List<NameValuePair> params) {

        HttpEntity httpEntity = null;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(Config.ADMIN_PANEL_URL + "/api/update_user_photo");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            httpEntity = httpResponse.getEntity();
            String entityResponse = EntityUtils.toString(httpEntity);
            Log.e("Entity Response  : ", entityResponse);
            return true;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
