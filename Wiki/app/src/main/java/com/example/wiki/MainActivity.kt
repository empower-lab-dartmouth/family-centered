package com.example.wiki

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {
    val query = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.queryButton)
        val editText = findViewById<EditText>(R.id.textView)
        var keyword = "";
        button.setOnClickListener {
            // Getting the user input
            keyword = editText.text.toString()
            // Showing the user input
            //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            val def = findViewById<TextView>(R.id.defText)
            val img = findViewById<ImageView>(R.id.image)
            val defurl =
                "https://simple.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&titles=${keyword}&exsentences=2&exintro=1&explaintext=1&exsectionformat=plain"
            val imgpath =
                "https://simple.wikipedia.org/w/api.php?action=query&format=json&origin=*&prop=pageimages&titles=${keyword}&piprop=original"

            val queue = Volley.newRequestQueue(this)
            val imgRequest = JsonObjectRequest(Request.Method.GET, imgpath, null,
                Response.Listener { response ->
                    val query = response.getJSONObject("query")
                    val pages = query.getJSONObject("pages")
                    for (key in pages.keys()) {
                        val num = pages.getJSONObject(key)
                        val original = num.getJSONObject("original")
                        val imgurl = original.getString("source")
                        //Loading image using Picasso
                        Picasso.get().load(imgurl).into(img)
                    }

                },
                Response.ErrorListener { error ->

                }
            )
            queue.add(imgRequest)
            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, defurl, null,
                Response.Listener { response ->
                    val query = response.getJSONObject("query")
                    val pages = query.getJSONObject("pages")
                    for (key in pages.keys()) {
                        val num = pages.getJSONObject(key)
                        val ans = num.getString("extract")
                        def.text = ans
                    }

                },
                Response.ErrorListener { error ->
                    def.text = error.toString()
                }
            )
           // queue.add(imgRequest)
            queue.add(jsonObjectRequest)

        }
    }

//    inner class AsyncTaskHandleJason : AsyncTask<String, String, String>() {
//        override fun doInBackground(vararg url: String?): String {
//            var text : String
//            val connection = URL(url[0]).openConnection() as HttpURLConnection
//            try {
//                connection.connect()
//                text = connection.inputStream.use{ it.reader().use{reader -> reader.readText()}}
//            } finally {
//                connection.disconnect()
//            }
//            return text;
//            }
//    }
}