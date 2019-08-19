package com.example.myfirstapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

//9
//placeholder page
public class Quest4Activity extends BasicFunctionalityPart2{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest4);

        View myLayout = findViewById(R.id.quest4_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ);
        View button = myLayout.findViewById(R.id.buttonQ);
        button.setVisibility(View.INVISIBLE);

        textView.setText("One day, there will be a real quest here. For now, congratulations! You've finished all the quests!!! We'll take you back to home base shortly :)");

        final Intent intent = new Intent(this, BeginQuest.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                editor.putBoolean(QUEST4_KEY, true);
                editor.commit();
                startActivity(intent);
            }
        }, 10000);
    }
}
