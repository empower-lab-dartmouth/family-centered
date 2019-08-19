package com.example.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

//4
//Placeholder for an account login page.
public class AccountPageActivity extends BasicFunctionality {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);
    }

    public void sendMessage1(View view) { //parents "login" button
        Intent intent = new Intent(this, IntroSequenceActivity2.class);
        startActivity(intent);
    }

    public void sendMessage2(View view) { //kids "login" button
        Intent intent = new Intent(this, IntroSequenceActivity2.class);
        startActivity(intent);
    }

    public void goToWikiPage(View view) { // Changes page to the vision page
        Intent intent = new Intent(this, VisionApi.class);
        startActivity(intent);
    }

    public void goToVisionPage(View view) { // Changes page to the vision page
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }
}
