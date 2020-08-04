package com.example.wiki;

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    Button button;
    EditText editText;
    String keyword;
    TextView def;
    ImageView img;

    RequestQueue queue;

    Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.queryButton);
        editText = findViewById(R.id.textView);
        keyword = "";
    }

    public void searchQuery(View view) {
        thisContext = this;
        search();
    }

    private void search(){

        new AsyncTask<Object, Void, Void>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Void doInBackground(Object... params) { //TODO: Once all apis are implemented, this should return the full image data structure that we want to build (A map of colors to objects)
                keyword = editText.getText().toString();

                String clue = getClueFromWiki(keyword);

                if (clue != null) def.setText(clue);
                else def.setText("Error");

                return null;
            }
        }.execute();
    }

    /**
     * Queries wikipedia and handles the response in order to return a "general knowledge clue" for an object label
     * @param keyword (ex: "stop sign")
     * @return clue (ex: "is a sign which is often met at crossroads, when a road does not have traffic lights.")
     */
    private String getClueFromWiki(String keyword){
        def = findViewById(R.id.defText);
        img = findViewById(R.id.image);
        String defurl = "https://simple.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&titles=" + keyword + "&exsentences=2&exintro=1&explaintext=1&exsectionformat=plain";

        queue = Volley.newRequestQueue(thisContext);

        String ans = makeSyncWikiRequest(defurl);
        String clue = parseOutClue(ans);
        return clue;
    }

    /**
     * Parses the definition returned from Simple Wikipedia to be in a form that can be easily added to the String "I spy something that..."
     * @param def (ex: "A mountain is a large natural rise of the Earth's surface that usually has a "summit" (the name for a mountain's top, which can also be called a peak).")
     * @return clue (ex: "is a large natural rise of the Earth's surface that usually has a "summit".")
     */
    private String parseOutClue(String def){
        String clue = "";

        //Remove anything in paranthesis using regex
        if (def.contains("(")){
            def = def.replaceAll("\\(.*\\)", "");
        }

        //parse def, starting at the index of the first occurance of "is" or "are"
        int isIndex = def.indexOf("is");
        int areIndex = def.indexOf("are");
        int clueStartIndex;

        if (isIndex != -1 && areIndex == -1){ // "is" is in def
            clueStartIndex = isIndex;
        } else if (areIndex != -1 && isIndex == -1){ // "are" is in def
            clueStartIndex = areIndex;
        } else if (isIndex != -1 && areIndex != -1) { // both "is" and "are" are in def
            clueStartIndex = isIndex < areIndex ? isIndex : areIndex;
        } else {
            clueStartIndex = -1; //Neither "is" nor "are" is in def
        }

        if (clueStartIndex != -1) {
            clue = def.substring(clueStartIndex, (def.indexOf('.') + 1));
        }

        return clue;
    }


    /**
     * Makes a synchronized Volley request to Simple Wikipedia
     */
    private String makeSyncWikiRequest(String defurl){
        queue = Volley.newRequestQueue(thisContext);

        //Setup a RequestFuture object
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        //Pass the future into the JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, defurl, null, future, future);

        //Add the request to the Request Queue
        queue.add(request);
        try {
            //Set an interval for the request to timeout. This will block the //worker thread and force it to wait for a response for 60 seconds //before timing out and raising an exception
            JSONObject response = future.get(60, TimeUnit.SECONDS);

            try {
                JSONObject query = response.getJSONObject("query");
                JSONObject pages = query.getJSONObject("pages");

                Iterator<String> keys = pages.keys();
                if (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject num = pages.getJSONObject(key);
                    String ans = num.getString("extract");
                    return ans;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // exception handling
        }
        return null;

    }

    private JsonObjectRequest makeImageRequest(String imgpath){
        JsonObjectRequest imgRequest = new JsonObjectRequest
                (Request.Method.GET, imgpath, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject query = response.getJSONObject("query");
                            JSONObject pages = query.getJSONObject("pages");

                            Iterator<String> keys = pages.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject num = pages.getJSONObject(key);
                                JSONObject original = num.getJSONObject("original");
                                String imgurl = original.getString("source");
                                Picasso.get().load(imgurl).into(img);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("ERROR", "Error occurred ", error);
                    }
                });
        return imgRequest;
    }

    private JsonObjectRequest makeDefRequest(String defurl){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, defurl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject query = response.getJSONObject("query");
                            JSONObject pages = query.getJSONObject("pages");

                            Iterator<String> keys = pages.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject num = pages.getJSONObject(key);
                                String ans = num.getString("extract");
                                def.setText(ans);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("ERROR", "Error occurred ", error);

                    }
                });
        return jsonObjectRequest;
    }
}


