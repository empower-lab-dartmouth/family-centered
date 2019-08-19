package com.example.myfirstapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/*
        Thanks to bofredo from stack for the idea of having a widely used parent activity
        https://stackoverflow.com/questions/20051174/how-do-i-share-common-functions-and-data-across-many-activities-in-a-single-andr
        Thanks to Onur Cevik and Mrad Mrad from stack for the idea about how a button could be added to the existing action bar
        https://stackoverflow.com/questions/38158953/how-to-create-button-in-action-bar-in-android
        https://stackoverflow.com/questions/33328486/how-to-add-icons-in-actionbar-when-its-setsupportactionbar
*/

//adds home button functionality to activities and makes storing information easier (currently it's just being stored in a local file)
public class BasicFunctionality extends AppCompatActivity {
    Menu menu;
    SharedPreferences pref; //stores information (ex player name, whether a quest is unlocked). Currently wiped every time app restarts from MainActivity
    SharedPreferences.Editor editor;
    final String PLAYER_NAME_KEY = "player_name";
    final String DRAGON_NAME_KEY = "dragon_name";
    final String PLAYER_NAME_DEFAULT = "player";
    final String DRAGON_NAME_DEFAULT = "Grumpy";
    final String QUEST2_KEY="quest2";
    final String QUEST3_KEY="quest3";
    final String QUEST4_KEY="quest4";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    //Home button functionality
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            startActivityAfterCleanup(IntroSequenceActivity1.class); //can be changed to have home button take user to a different point
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startActivityAfterCleanup(Class<?> cls) {
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

