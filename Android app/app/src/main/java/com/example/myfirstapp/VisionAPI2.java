package com.example.myfirstapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Scanner;



public class VisionAPI2 extends BasicFunctionality{

    public static final String SERVER_GET = "http://10.0.2.2:3000/";
    public static final String SERVER_POST = "http://10.0.2.2:3000/search_wiki";
    private TextView tvServerResponse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_api2);

        // Makes it so that the keyboard does not open when activity is started.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        tvServerResponse = findViewById(R.id.nodeText);
        Button contactServerButton = findViewById(R.id.requestServerButton);
        tvServerResponse.setMovementMethod(new ScrollingMovementMethod());

        EditText wikiTextEntry = (EditText) findViewById(R.id.wikiTextEntry);
        wikiTextEntry.setText("");
        wikiTextEntry.setHint("Enter a word");

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
                nodeText.setText("Processing Query!");

                // Makes keyboard closes when button is clicked.
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }

                // Connects to the node code to get the wiki entry
                VisionAPI2.HttpPostRequest requestPost = new VisionAPI2.HttpPostRequest();
                requestPost.execute();

//                HttpGetRequest request = new HttpGetRequest();
//                request.execute();
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
            tvServerResponse.setText(result);
        }
    }

    public class HttpPostRequest extends AsyncTask<Void, Void, String> {

        static final String REQUEST_METHOD = "POST";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;
        EditText wikiQuery   = (EditText)findViewById(R.id.wikiTextEntry);
        String postData = wikiQuery.getText().toString();

        @Override
        protected String doInBackground(Void... params){
            StringBuffer response = new StringBuffer();
            String inputLine;
            HttpURLConnection connection = null;

            try {
                // connect to the server
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
            tvServerResponse.setText(result);
        }
    }




}
