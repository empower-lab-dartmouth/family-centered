package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

public class WikiClueAPI {

    /**
     * Queries wikipedia and handles the response in order to return a "general knowledge clue" for an object label
     * @param keyword (ex: "stop sign")
     * @return clue (ex: "is a sign which is often met at crossroads, when a road does not have traffic lights.")
     */
    public static String getWikiClue(String keyword, Context thisContext){
        String clue = null;
        String defurl = "https://simple.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&titles=" + keyword + "&exsentences=2&exintro=1&explaintext=1&exsectionformat=plain";

        String def = makeSyncWikiRequest(defurl, thisContext);
        if (def != null) {
            clue = parseOutClue(def);
        }
        return clue;
    }


    /**
     * Parses the definition returned from Simple Wikipedia to be in a form that can be easily added to the String "I spy something that..."
     * @param def (ex: "A mountain is a large natural rise of the Earth's surface that usually has a "summit" (the name for a mountain's top, which can also be called a peak).")
     * @return clue (ex: "is a large natural rise of the Earth's surface that usually has a "summit".")
     */
    private static String parseOutClue(String def){
        String clue = null;

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
    private static String makeSyncWikiRequest(String defurl, Context thisContext){
        RequestQueue queue = Volley.newRequestQueue(thisContext);

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
}
