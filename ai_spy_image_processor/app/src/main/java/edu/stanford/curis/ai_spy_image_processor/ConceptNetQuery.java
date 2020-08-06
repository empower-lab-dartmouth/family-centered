package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

public class ConceptNetQuery extends AsyncTask<Void, Void, ArrayList<String>> {

    private String target;
    private String relation;
    private Context context;
    private CountDownLatch latch;
    private HashMap<String, ArrayList<String>> map;

    private final int MAX_NUM_RESULTS = 3;

    public ConceptNetQuery (String target, String relation, HashMap<String, ArrayList<String>> map, Context context, CountDownLatch latch){
        this.target = target;
        this.relation = relation;
        this.context = context;
        this.latch = latch;
        this.map = map;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params){
        ArrayList<String> results = queryConceptNet();
//        latch.countDown();
        return results;
    }

    @Override
    protected void onPostExecute(ArrayList<String> results){
        if (results.size() > 0){
            map.put(this.relation, results);
        }
        latch.countDown();
    }

    private ArrayList<String> queryConceptNet(){

        ArrayList<String> results = new ArrayList<>();

        String queryUrl = "http://api.conceptnet.io/query?node=/c/en/" + this.target + "&rel=/r/" + this.relation;

        RequestQueue queue = Volley.newRequestQueue(context);

        //Setup a RequestFuture object
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        //Pass the future into the JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, queryUrl, null, future, future);

        //Add the request to the Request Queue
        queue.add(request);
        try {
            //Set an interval for the request to timeout. This will block the //worker thread and force it to wait for a response for 60 seconds //before timing out and raising an exception
            JSONObject response = future.get(60, TimeUnit.SECONDS);

            try {
                JSONArray edges = response.getJSONArray("edges");

                int numResults = 0;
                for (int i = 0; i < edges.length() && numResults < MAX_NUM_RESULTS; i++){
                    JSONObject edge = (JSONObject) edges.get(i);
                    JSONObject end = (JSONObject) edge.get("end");
                    String label = (String) end.get("label");
                    if (!label.contains(target)){
                        results.add(label);
                        numResults++;
                    }
                }
                return results;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // exception handling
        }
        return null;
    }
}

