package com.example.myfirstapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myfirstapp.OpeningAnimation;

import java.util.ArrayList;
import java.util.Arrays;

//designed for galaxy 10 on tablet, samsung galaxy tab s4

/*
Thanks to androidstudio tutorial for help with animations
https://developer.android.com/training/animation/reposition-view
Thanks to TWilly from slack for animation help
https://stackoverflow.com/questions/6796139/fade-in-fade-out-android-animation-in-java
Thanks to hackbod from slack for help on getting orientation information
https://stackoverflow.com/questions/2795833/check-orientation-on-android-phone
Thanks to user2045814 from slack for how to handle orientation changes
https://stackoverflow.com/questions/17184047/start-new-activity-on-orientation-change
 */

//1
//the numbers in an activity mark where in the interaction sequence it appears.
// MainActivity is 1, as shown above, IntroSequenceActivity1 is 2, etc.

public class MainActivity extends AppCompatActivity {
    public int currentStep = 0;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear(); //here for testing purposes. Eventually, should store values on firebase
        editor.commit();

        //timing values for the opening animation sequence
        ArrayList<Integer> ts = new ArrayList<Integer>(
                Arrays.asList(1500, 1500, 1500, 1500, 1500, 0, 750, 750, 750, 1500, 1000, 1000, 2500, 800, 800, 3500, 3000, 3000, 4300, 1000, 6300)); //all delay and duration timings, in order
        int orientation = getResources().getConfiguration().orientation;
        OpeningAnimation.runAnimations((ImageView) findViewById(R.id.imageView6), (ImageView) findViewById(R.id.imageView7), findViewById(R.id.mainbutton), getResources().getDrawable(R.drawable.dragon2), getResources().getConfiguration().orientation, ts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        this.menu = menu;
        MenuItem item = menu.findItem(R.id.mybutton);
        item.setIcon(getResources().getDrawable(R.drawable.egg));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            startActivityAfterCleanup(IntroSequenceActivity1.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startActivityAfterCleanup(Class<?> cls) {
        //Intent intent = new Intent(getApplicationContext(), cls);
        Intent intent = new Intent(getApplicationContext(), IntroSequenceActivity1.class);
        startActivity(intent);
    }

    public void sendMessage(View view) {
       Intent intent = new Intent(this, AccountPageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        startActivityAfterCleanup(IntroSequenceActivity1.class);
    }
}
