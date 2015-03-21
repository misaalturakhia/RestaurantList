package com.misaal.coupondunia;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/** An AsyncTask that fetches restaurant data from the stored url in the form of JSON. The JSON is also
 * read, parsed and the needed data is extracted into a List<Restaurant>
 * Created by Misaal on 19/03/2015.
 */
public class FetchNetDataTask extends AsyncTask<Void, Void, List<Restaurant>> {

    private static final String LOG_TAG = FetchNetDataTask.class.getSimpleName();
    private static final String DATA_URL = "http://staging.couponapitest.com/task_data.txt";
    private static final int TIMEOUT = 15000; // 15 SECONDS
    private final Context mContext;


    /**
     * Constructor
     * @param context : Context
     */
    public FetchNetDataTask(Context context){
        this.mContext = context;
    }


    @Override
    protected void onPreExecute() {
        // check if the device is connected to the internet
        if(!isNetworkConnected()){
            Toast.makeText(mContext, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            cancel(true); // if not connected cancel the task
        }
    }


    @Override
    protected List<Restaurant> doInBackground(Void... voids) {
        String jsonStr = null;
        try{
            URL u = new URL(DATA_URL);
            HttpURLConnection c = createConnection(u);
            c.connect();
            int status = c.getResponseCode();
            switch (status) {
                case 200: // SUCCESS CODES
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    jsonStr = sb.toString();
            }
            c.disconnect();
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "Invalid URL!");
            cancel(true);
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Could not connect to URL due to network issues");
            cancel(true);
        }

        List<Restaurant> restaurants = null;
        if(jsonStr != null){
            // parse JSON string
            JSONReader reader = new JSONReader(jsonStr);
            try {
                restaurants = reader.extractRestaurantList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return restaurants;
    }


    /**
     * Creates and sets parameters to a HttpURLConnection object
     * @param u
     * @return : httpUrlConnection
     * @throws IOException
     */
    private HttpURLConnection createConnection(URL u) throws IOException {
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod("GET");
        c.setUseCaches(false);
        c.setAllowUserInteraction(false);
        c.setConnectTimeout(TIMEOUT);
        c.setReadTimeout(TIMEOUT);
        return c;
    }


    /**
     * Checks if the phone has any active network connection and returns true or false
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;// There are no active networks.
        } else
            return true;
    }
}
