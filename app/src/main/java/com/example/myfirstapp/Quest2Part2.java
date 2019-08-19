package com.example.myfirstapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

//not currently being used. The code here is a hold over from an earlier design. Most likely won't be useful.
//just here as an example of how a "one template for all quests" design might be used
public class Quest2Part2 extends BasicFunctionality {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest2_part2);

        View myLayout = findViewById(R.id.quest_part2_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ2);
        textView.setText("Which animal would you like to ferry across the river first?");

        ImageView imageView = findViewById(R.id.imageViewQ1);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.lion));
        imageView = findViewById(R.id.imageViewQ2);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.goat));
        imageView = findViewById(R.id.imageViewQ3);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.wheat));
    }

    public void sendMessage1(View view){
        View myLayout = findViewById(R.id.quest_part2_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ2);
        textView.setText("Oooh good guess! But Loki points out... Try again!" );

        ImageView imageView = findViewById(R.id.imageViewQ1);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.lion));
    }

    public void sendMessage3(View view){
        View myLayout = findViewById(R.id.quest_part2_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ2);
        textView.setText("Oooh good guess! But Loki points out... Try again!" );

        ImageView imageView = findViewById(R.id.imageViewQ3);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.wheat));
    }

    public void sendMessage2(View view){
        View myLayout = findViewById(R.id.quest_part2_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ2);
        textView.setText("Nice work! Loki gives you a fragrant branch of mistletoe for your efforts.");

        ImageView imageView = findViewById(R.id.imageViewQ1);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.mistletoe));
        imageView = findViewById(R.id.imageViewQ2);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.blank_icon));
        imageView = findViewById(R.id.imageViewQ3);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.blank_icon));
    }

}
