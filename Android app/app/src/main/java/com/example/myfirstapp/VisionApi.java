package com.example.myfirstapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.webkit.JavascriptInterface;
import android.content.Context;


import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import android.util.Log;
import java.util.SortedSet;
import java.util.TreeSet;


import net.alhazmy13.wordcloud.WordCloud;
import net.alhazmy13.wordcloud.WordCloudView;

import com.google.api.client.json.Json;



public class VisionApi extends BasicFunctionality{

    public static String SERVER_GET = "http://10.0.2.2:3000/";
    public static String SERVER_POST = "http://10.0.2.2:3000/search_wiki";
    private TextView tvServerResponse;
    private TextView tvServerResponse2;
    public WordCloudView wordCloud;
    public static int flag;
    public static int flag2;

    public static String cloudText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_api);

        // Makes it so that the keyboard does not open when activity is started.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        tvServerResponse = findViewById(R.id.nodeText);
        Button contactServerButton = findViewById(R.id.requestServerButton);
        tvServerResponse.setMovementMethod(new ScrollingMovementMethod());
        tvServerResponse2 = findViewById(R.id.nodeText2);
        tvServerResponse2.setMovementMethod(new ScrollingMovementMethod());

        EditText wikiTextEntry = (EditText) findViewById(R.id.wikiTextEntry);
        wikiTextEntry.setText("");
        wikiTextEntry.setHint("Enter a word");

        wordCloud = (WordCloudView) findViewById(R.id.wordCloud);
        wordCloud.getSettings().setLoadWithOverviewMode(true);
        wordCloud.getSettings().setUseWideViewPort(true);

        contactServerButton.setOnClickListener(onButtonClickListener);
    }

    View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Makes sure a word is entered in the editText
            EditText wikiTextEntry = (EditText) findViewById(R.id.wikiTextEntry);
            if (TextUtils.isEmpty(wikiTextEntry.getText())){
                wikiTextEntry.setError("Please enter a word!");
            } else {
                // Let's the user know the app is processing the entry.
                TextView nodeText   = (TextView)findViewById(R.id.nodeText);
                nodeText.setText("Processing query!");

                // Makes keyboard closes when button is clicked.
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }

                //flag = 0;
                //flag2 = 0;
                cloudText = "";

                // Connects to the node code to get the wiki entry

                HttpPostRequest requestPost = new HttpPostRequest("http://10.0.2.2:2000/", "http://10.0.2.2:2000/search_wiki");
                requestPost.execute();

                HttpPostRequest requestPost2 = new HttpPostRequest("http://10.0.2.2:3000/", "http://10.0.2.2:3000/search_wiki");
                requestPost2.execute();
            }
        }
    };

    public class HttpGetRequest extends AsyncTask<Void, Void, String> { //https://medium.com/@suragch/minimal-client-server-example-for-android-and-node-js-343780f28c28 (I think)

        static final String REQUEST_METHOD = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(Void... params){
            String result;
            String inputLine;

            try {
                // connect to the server
                URL myUrl = new URL(SERVER_GET);
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.connect();

                // get the string from the input stream
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                reader.close();
                streamReader.close();
                result = stringBuilder.toString();

            } catch(IOException e) {
                e.printStackTrace();
                result = "Request failed.";
            }
            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            //tvServerResponse.setText(result);
            //flag = (flag+1)%2;
            //if (flag == 1){
            if (SERVER_GET == "http://10.0.2.2:2000/") {
                tvServerResponse.setText(result);
            }
            else{
                tvServerResponse2.setText(result);
                cloudText = result;

                ArrayList<WordCloud> words = new ArrayList<WordCloud>();
                makeWordsList(words, cloudText);
                wordCloud.setDataSet(words);
                wordCloud.notifyDataSetChanged();
                Log.i("words2", "THIS WAS CALLED!");

            }


        }
    }

    // Attempt at post request

    public class HttpPostRequest extends AsyncTask<Void, Void, String> {

        static final String REQUEST_METHOD = "POST";
        static final int READ_TIMEOUT = 15000;
        String SERVER_GET;
        String SERVER_POST;
        static final int CONNECTION_TIMEOUT = 15000;
        EditText wikiQuery   = (EditText)findViewById(R.id.wikiTextEntry);
        String postData = wikiQuery.getText().toString();

        public HttpPostRequest(String GetStr, String PostStr){
            this.SERVER_GET = GetStr;
            this.SERVER_POST = PostStr;
        }

        @Override
        protected String doInBackground(Void... params){
            StringBuffer response = new StringBuffer();
            HttpURLConnection connection = null;

            try {
                URL myUrl = new URL(SERVER_POST);
                connection =(HttpURLConnection) myUrl.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.connect();

                // Send post request
                DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
                wr.writeBytes(postData);
                wr.flush ();
                wr.close ();

                if (connection.getResponseCode() ==  HttpURLConnection.HTTP_OK) {
                    //Get Response
                     InputStream is = connection.getInputStream();
                     BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                     String line;
                    while((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\n');
                    }	        rd.close();
                } else {
                    response.append("HTTP Response code not OK - " + connection.getResponseCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return response.toString();
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            //tvServerResponse.setText(result);
            //flag = (flag+1)%2;
            //if (flag == 1){
            if (SERVER_GET == "http://10.0.2.2:2000/") {
                tvServerResponse.setText(result);
            }
            else {
                tvServerResponse2.setText(result);
                cloudText = result;

                ArrayList<WordCloud> words = new ArrayList<WordCloud>(); //THE WORD CLOUD IS SCROLLABLE
                makeWordsList(words, cloudText);
                wordCloud.setDataSet(words);
                wordCloud.notifyDataSetChanged();
                Log.i("words2", cloudText);

            }

        }
    }

    public void makeWordsList(ArrayList<WordCloud> words, String result){
        //result = result.toLowerCase();

        Map<String, Integer> map = new HashMap<>();
        String[] lst = result.split(" ");

        String[] lst2 = {"the","and","in","to","too","then","it","an","on","is","was","are","were","a","its","as","it's", "they", "them"};
        Set<String> stopwords = new HashSet<String>();
        for (String w : lst2){
            stopwords.add(w);
        }

        for (String w : lst) {
            if (stopwords.contains(w)){
                continue;
            }
            w = w.replace("(","");
            w = w.replace(")","");
            w = w.replace(".","");
            w = w.replace(",","");
            w = w.replace(":","");
            w = w.replace(";","");
            w = w.replace("!","");
            Integer n = map.get(w);
            n = (n == null) ? 1 : ++n;
            map.put(w, n);
        }


        Map<String, Integer> map2 = new HashMap<>();
        int threshold = 20;
        SortedSet<String> keys = new TreeSet<>(map.keySet()); //thanks stack and Jherico: https://stackoverflow.com/questions/922528/how-to-sort-map-values-by-key-in-java
        for (String key : keys) {
            int val = map.get(key);
            map2.put(key, val/5);
            if (map2.size() > threshold) {
                break;
            }
        }


        for (String w : map2.keySet()) {
            words.add(new WordCloud(w, map2.get(w)));
        }

    }


}
