package com.example.wiki;

import android.content.ContentProviderClient;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MainActivity1 extends AppCompatActivity {
    Button button;
    EditText editText;
    String keyword;
    TextView def;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.queryButton);
        editText = findViewById(R.id.textView);
        keyword = "";
    }

    public void searchQuery(View view) {
        keyword = editText.getText().toString();
        def = findViewById(R.id.defText);
        img = findViewById(R.id.image);
        String defurl = "https://simple.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&titles=" + keyword + "&exsentences=2&exintro=1&explaintext=1&exsectionformat=plain";
        String imgpath = "https://simple.wikipedia.org/w/api.php?action=query&format=json&origin=*&prop=pageimages&titles=" + keyword + "&piprop=original";

        RequestQueue queue = Volley.newRequestQueue(this);
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

        queue.add(imgRequest);

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
        queue.add(jsonObjectRequest);
    }
}


//        queue.add(imgRequest)
//        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, defurl, null,
//                Response.Listener { response ->
//                val query = response.getJSONObject("query")
//            val pages = query.getJSONObject("pages")
//            for (key in pages.keys()) {
//                val num = pages.getJSONObject(key)
//                val ans = num.getString("extract")
//                def.text = ans
//            }
//
//        },
//        Response.ErrorListener { error ->
//                def.text = error.toString()
//        }
//            )
//        // queue.add(imgRequest)
//        queue.add(jsonObjectRequest)
//
//    }
//}
