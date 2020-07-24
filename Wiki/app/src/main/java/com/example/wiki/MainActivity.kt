package com.example.wiki

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    val query = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.queryButton)
        val editText = findViewById<EditText>(R.id.textView)
        var text = "";
        button.setOnClickListener {
            // Getting the user input
            text = editText.text.toString()
            // Showing the user input
            //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            val def = findViewById<TextView>(R.id.defText)
            val url =
                "https://simple.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&titles=${text}&exsentences=2&exintro=1&explaintext=1&exsectionformat=plain"
            val queue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
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
            queue.add(jsonObjectRequest)
// Access the RequestQueue through your singleton class.
            //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

//            AsyncTaskHandleJason().execute(url)
//            val jsonObjectRequest = JsonObjectRequest(
//                Request.Method.GET, url, null,
//                Response.Listener { response ->
//                    def.text = "hi"
//                    Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
//                },
//                Response.ErrorListener { error ->
//                    def.text = "GET request failed"
//                }
//            )
        }

//        button.setOnClickListener(new View.onClickListener() {
//            public void onClick(View v) {
//                openActivity( )
//            }
//        });
//    }


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