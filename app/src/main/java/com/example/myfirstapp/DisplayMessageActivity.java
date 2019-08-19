package com.example.myfirstapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

//6

public class DisplayMessageActivity extends BasicFunctionality {
    String playerName;
    String dragonName;
    static boolean movedOn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Makes it so that the keyboard does not open when activity is started.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        playerName = pref.getString(PLAYER_NAME_KEY, PLAYER_NAME_DEFAULT);

        TextView textView = findViewById(R.id.textView);
        textView.setText("Hello " + playerName + "! My name is Aragornigrumpsivitch.\n" +
                "\n" +
                "Maybe you want to give me a nickname?");

        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText("");
        editText.setHint("Enter a nickname here");



        //mechanism to prevent moveOn() from being called during the next activity
        movedOn = false;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!movedOn) moveOn();
            }
        }, 20000); //changed from 10 seconds (Bethanie's design) --> 20 seconds

    }

    public void sendMessage(View view) {
        movedOn = true;

        EditText editText = (EditText) findViewById(R.id.editText);
        if (TextUtils.isEmpty(editText.getText())){
            editText.setError("Please enter a nickname!");
        } else {
            //stores the entered dragon name
            dragonName = editText.getText().toString();
            editor.putString(DRAGON_NAME_KEY, dragonName);
            editor.commit();

            TextView textView = findViewById(R.id.textView);
            textView.setText("'"+ dragonName + "!'\n" + "Yay. I love my nickname. Tap my egg any time and we can talk.");

            // Makes keyboard closes when button is clicked.
            try {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                e.fillInStackTrace();
            }

            //makes button invisible so that two names are not submitted
            editText.setVisibility(View.INVISIBLE);
            View button = findViewById(R.id.button);
            button.setVisibility(View.INVISIBLE);

            final Intent intent = new Intent(this, BeginQuest.class);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                } //starts next activity (after a 5 second delay)
            }, 5000);
        }
    }

    //called if a player has not named the dragon after some amount of time (specified earlier)
    public void moveOn() {
        dragonName = DRAGON_NAME_DEFAULT;
        movedOn = true;

        editor.putString(DRAGON_NAME_KEY, dragonName);
        editor.commit();

        Intent intent = new Intent(this, BeginQuest.class);
        startActivity(intent);
    }

}
