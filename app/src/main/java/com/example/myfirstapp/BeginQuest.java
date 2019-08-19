package com.example.myfirstapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

//7
//LAYOUTS ARE NOT HORIZONTALLY ADAPTABLE FROM THIS POINT ONWARDS

//general note: the design of the interaction from this class onwards mostly departs from the figma
public class BeginQuest extends BasicFunctionality {
    String playerName;
    String dragonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_quest);

        //not currently using either the player or the dragon's names in this activity, but this is how you would access them
        playerName = pref.getString(PLAYER_NAME_KEY, PLAYER_NAME_DEFAULT);
        dragonName = pref.getString(DRAGON_NAME_KEY, DRAGON_NAME_DEFAULT);

        final TextView textView = findViewById(R.id.textView2);

        textView.setText("Let's explore the world together!" + "\n" +  "Click on any unlocked quests to go on an adventure!");

        //unlocks quests as appropriate (makes icons clickable, not greyed out)
        String[] keys = {QUEST2_KEY, QUEST3_KEY, QUEST4_KEY};
        int[] ids = {R.id.imageView3, R.id.imageView16, R.id.imageView5, R.id.imageView17, R.id.imageView14, R.id.imageView18};
        Drawable[] ds = {getResources().getDrawable(R.drawable.fairy), getResources().getDrawable(R.drawable.sun), getResources().getDrawable(R.drawable.unicorn)};
        for (int i = 0; i < keys.length; i++){
            String k = keys[i];
            boolean b = pref.getBoolean(k, false);
            if (b) {
                ImageView imageView = findViewById(ids[2*i]);
                imageView.setImageDrawable(ds[i]);
                imageView = findViewById(ids[2*i+1]);
                imageView.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void sendMessage1(View view) {
        Intent intent = new Intent(this, MapsActivityQ1.class);
        startActivity(intent);
    }

    public void sendMessage2(View view) {
        sendMessageInner(view, QUEST2_KEY, MapsActivityQ2.class);
    }

    public void sendMessage3(View view) {
        sendMessageInner(view, QUEST3_KEY, MapsActivityQ3.class);
    }

    public void sendMessage4(View view) {
        sendMessageInner(view, QUEST4_KEY, MapsActivityQ4.class);
    }

    //checks that the quest the user has clicked on is actually unlocked
    public void sendMessageInner(View view, String k, Class c){
        boolean b = pref.getBoolean(k, false);
        if (!b){
            TextView textView = findViewById(R.id.textView2);
            textView.setText("Sorry, that quest has not been unlocked." + "\n" +  "Click on any unlocked quests to go on an adventure!");
            return;
        }
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }



}
