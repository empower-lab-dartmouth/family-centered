package com.example.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/*
Thanks to Azhar Shaikh from stack for info on how to handle screen rotations without losing data
https://stackoverflow.com/questions/10126845/handle-screen-rotation-without-losing-data-android
Thanks to Sotti from stack for how to set min api level
https://stackoverflow.com/questions/19465049/changing-api-level-android-studio
Thanks to CommonsWare from stack for where to place assets folder
https://stackoverflow.com/questions/18302603/where-do-i-place-the-assets-folder-in-android-studio
Thanks to Nikhilreddy Gujjula from stack for how to dynamically set images
https://stackoverflow.com/questions/8642823/using-setimagedrawable-dynamically-to-set-image-in-an-imageview
Thanks to Kartheek from stack for how to delay execution of a function
https://stackoverflow.com/questions/31041884/execute-function-after-5-seconds-in-android
Thanks to androidstudio tutorial
https://developer.android.com/training/basics/firstapp/?authuser=0
Thanks to Muhammad Ali from coding cafe (on youtube) for how to scroll automtically
https://www.youtube.com/watch?v=57WwFl7dDr4
Thanks to Vlad Ivchenko from slack for how to scroll automatically (for realsies)
https://stackoverflow.com/questions/42227250/android-studio-automatic-scroll-to-bottom-when-scrollview-expands
 */

//3

public class IntroSequenceActivity2 extends AppCompatActivity {
    public int currentStep = 0; //keeps track of where in the sequence the user is. Is stored so it doesn't reset after a tablet rotation.
    Menu menu;
    HorizontalScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_sequence2);
        sendMessageInner();

        TextView messageView = findViewById(R.id.chatBox);
        mScrollView = findViewById(R.id.chatBoxOuter);
        messageView.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        this.menu = menu;
        MenuItem item = menu.findItem(R.id.mybutton);
        item.setIcon(getResources().getDrawable(R.drawable.egg));
        return super.onCreateOptionsMenu(menu);
    }

    public void sendMessage(View view) {
        currentStep++;
        sendMessageInner();
    }

    //manages sequence of display changes
    public void sendMessageInner(){
        if (currentStep == 0) displayChange0();
        if (currentStep == 1) displayChange1();
        if (currentStep == 2) displayChange2();
        if (currentStep == 3) displayChange3();
        if (currentStep == 4) displayChange4();
        if (currentStep == 5) {
            Intent intent = new Intent(this, EnterNameActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentStep", currentStep);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentStep = savedInstanceState.getInt("currentStep"); //Restoring currentStep
        sendMessageInner();
    }

    public void displayChange0() {
        TextView textView = findViewById(R.id.textView4);
        textView.setText("This is a world of quests... waiting for a hero to complete them.");
    }

    public void displayChange1(){
        TextView textView = findViewById(R.id.textView4);
        textView.setText("Some quests are hard to find. Some quests are hard to unlock! It's best if you have a companion... Shake the tablet gently to crack open the egg.");
        //currently continue button (not shake event)

        TextView messageView = findViewById(R.id.chatBox);
        messageView.append("This is a world of quests... waiting for a hero to complete them.");

    }

    public void displayChange2(){
        TextView textView = findViewById(R.id.textView4);
        textView.setText("Why, it's a baby dragon! Dragons are super intelligent, and they have a good nose for treasure."); //C: got rid of "Tap your dragon gently."


        TextView messageView = findViewById(R.id.chatBox);
        messageView.append("Some quests are hard to find. Some quests are hard to unlock! It's best if you have a companion... Shake the tablet gently to crack open the egg.");

        //scrolling functionality (so the user can see previous instructions)
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });

        ImageView imageView = findViewById(R.id.imageView2);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dragon));

        MenuItem item = menu.findItem(R.id.mybutton);
        item.setIcon(getResources().getDrawable(R.drawable.dragon2));

    }

    public void displayChange3(){
        TextView textView = findViewById(R.id.textView4);
        textView.setText("Dragons have special powers. They can see things humans can't, and they can remember things their parents knew. Your dragon can see secrets hiding in plain sight.");

        TextView messageView = findViewById(R.id.chatBox);
        messageView.append("Why, it's a baby dragon! Dragons are super intelligent, and they have a good nose for treasure. \t");

        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });

        ImageView imageView = findViewById(R.id.imageView2);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dragon));
    }

    public void displayChange4(){
        TextView textView = findViewById(R.id.textView4);
        textView.setText("Using this tablet, you can see what your baby dragon sees. When it remembers things, it will give you hints. Work together to unlock the hidden treasures around you.");

        TextView messageView = findViewById(R.id.chatBox);
        messageView.append("Dragons have special powers. They can see things humans can't, and they can remember things their parents knew. Your dragon can see secrets hiding in plain sight. \t");

        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });

        ImageView imageView = findViewById(R.id.imageView2);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dragon));
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
        Intent intent = new Intent(getApplicationContext(), cls);
        startActivity(intent);
    }

}
