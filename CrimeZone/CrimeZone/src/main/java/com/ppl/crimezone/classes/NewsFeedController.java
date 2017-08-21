package com.ppl.crimezone.classes;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajiutamaa on 08/05/14.
 */
public class NewsFeedController {
    private List<NewsFeedItem> newsItemList;

    public NewsFeedController(){
        newsItemList = new ArrayList<NewsFeedItem>();
    }

    public List<NewsFeedItem> getNewsList(){
        return newsItemList;
    }

    public boolean fetchNewsFeedItem(int limit){
        JSONObject jsonObject = null;

        boolean success = false;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://crimezone.besaba.com/webservice/newsFeed.php");
        try{

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("limit", String.valueOf(limit)));

            //Create HTTP request
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);

            StatusLine feedRequestStatus = response.getStatusLine();

            //Connection success
            Log.d("Connection status", feedRequestStatus.getStatusCode()+"");
            if(feedRequestStatus.getStatusCode() == 200){
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

                jsonObject = GsonParser.parseToJSON(content);
                JSONArray jsonArray = new JSONArray(jsonObject);

                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject element = jsonArray.getJSONObject(i);
                    String title = element.getString("title");
                    String reportID = element.getString("reportID");
                    String username = element.getString("username");
                    String time = element.getString("TIME");
                    String rating = element.getString("avg_rating");

                    JSONArray categoryList = element.getJSONArray("CategoryName");
                    int [] categories = new int[categoryList.length()];
                    for(int j = 0; j < categoryList.length(); j++){
                        categories[j] = categoryList.getInt(j);
                    }

                    NewsFeedItem item = new NewsFeedItem(title, username, reportID, time, rating, categories);
                    newsItemList.add(item);
                }
            }

        } catch(Exception e){

        }

        return success;
    }
}
