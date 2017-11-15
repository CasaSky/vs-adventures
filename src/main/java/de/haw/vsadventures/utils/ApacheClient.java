package de.haw.vsadventures.utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ApacheClient {

    private HttpClient httpClient = HttpClientBuilder.create().build();

    private HttpPost httpPost;
    private HttpGet httpGet;

    private final Header applicationHeader = new BasicHeader("content-type", "application/json");
    private final String AUTHORIZATION = "Authorization";

    public String post(String url, String loginToken, String request, String reponseAttribute) throws Exception {

        httpPost = new HttpPost(url);
        httpPost.addHeader(applicationHeader);
        httpPost.addHeader(AUTHORIZATION, "Token {"+loginToken+"}");

        try {

            httpPost.setEntity(new StringEntity("{" + request + "}"));

            HttpResponse response = httpClient.execute(httpPost);
            String json = EntityUtils.toString(response.getEntity());

            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString(reponseAttribute);
        } catch (Exception e) {

            try {
                throw e;
            } catch (IOException e1) {
                throw e1;
            } catch (JSONException e2) {
                throw e2;
            }
        }
    }

    public String get(String url, String loginToken, String responseAttribute) throws Exception {

        httpGet = new HttpGet(url);
        httpGet.addHeader(applicationHeader);
        httpGet.addHeader(AUTHORIZATION, "Token {"+loginToken+"}");

        try {

            HttpResponse response = httpClient.execute(httpGet);
            String json = EntityUtils.toString(response.getEntity());

            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString(responseAttribute);
        } catch (Exception e) {

            try {
                throw e;
            } catch (IOException e1) {
                throw e1;
            } catch (JSONException e2) {
                throw e2;
            }
        }
    }
}
