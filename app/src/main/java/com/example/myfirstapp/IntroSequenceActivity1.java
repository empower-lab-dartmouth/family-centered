package com.example.myfirstapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

//2

public class IntroSequenceActivity1 extends AppCompatActivity {
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_sequence1);

        TextView messageView = findViewById(R.id.chatBox);
        messageView.setText("");

        //specifies timings for the opening animation sequence
        //currently, all the timings are set to 0, but feel free to alter this
        //In my opinion, it should still be some sort of abridged version of the MainActivity opening sequence, however
        ArrayList<Integer> ts = new ArrayList<Integer>();
        for (int x = 0; x < 21; x++) ts.add(0);
        OpeningAnimation.runAnimations((ImageView) findViewById(R.id.imageView8), (ImageView) findViewById(R.id.imageView9), findViewById(R.id.mainbutton2), getResources().getDrawable(R.drawable.dragon2), getResources().getConfiguration().orientation, ts);

    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, AccountPageActivity.class);
        startActivity(intent);
    }

    //the following function is different from the BasicFunctionality version
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        this.menu = menu;
        MenuItem item = menu.findItem(R.id.mybutton);
        item.setIcon(getResources().getDrawable(R.drawable.egg));
        return super.onCreateOptionsMenu(menu);
    }

    //can make this class extend BasicFunctionality and delete the following four functions, probably
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
        Intent intent = new Intent(getApplicationContext(), cls);
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
}
