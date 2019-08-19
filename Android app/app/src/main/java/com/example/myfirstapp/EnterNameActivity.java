package com.example.myfirstapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/*
Thanks to Krausz Lóránt Szilveszter from slack for helping me to store key-val pairs
https://stackoverflow.com/questions/23024831/android-shared-preferences-example
Thanks to 'Will Tate' from stack for how to send information to next activity... though I'm no longer using his method
https://stackoverflow.com/questions/5265913/how-to-use-putextra-and-getextra-for-string-data
 */

//5

public class EnterNameActivity extends BasicFunctionality {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_name);

        TextView textView = findViewById(R.id.textView3);
        textView.setText("");

        EditText editText;
        editText = (EditText) findViewById(R.id.editText);
        editText.setText("");
        editText.setHint("Enter your name here");

        // Makes it so that the keyboard does not open when activity is started.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //this if statement is useless, can probably be deleted (I was hesitant to do so without the tablet to test on)
        /*if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            ImageView imageView = findViewById(R.id.imageView10);
        }*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                displayHint();
            }
        }, 20000); //changed from 10 seconds (as specified in Bethanie's design) --> 20 seconds
    }


    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        if (TextUtils.isEmpty(editText.getText())){
            editText.setError("Please Enter a username!");
        } else { //stores player's name (which the user entered)
            String message = editText.getText().toString();
            editor.putString(PLAYER_NAME_KEY, message);
            editor.commit();

            Intent intent = new Intent(this, DisplayMessageActivity.class);
            startActivity(intent);
        }
    }

    //nudges user to enter a name
    public void displayHint(){
        TextView textView = findViewById(R.id.textView3);
        textView.setText("Enter a name I can call you!");
    }

    //the following can be deleted, is redundant (I caught this after returning the tablet and was hesitant to delete without it)
    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }*/

}
