package com.example.myfirstapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

//9b

public class Quest1Part2Activity extends BasicFunctionalityPart2 {
    double correctAns = 1178.1;
    Menu menu;
    boolean solutionFound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest1_part2);

        View myLayout = findViewById(R.id.quest_part2_style);

        TextView textView = myLayout.findViewById(R.id.textViewQ2);
        textView.setText("Please enter the area to be painted, in square feet and rounded to one decimal place, in the form below. Remember, you've been hired to repaint the flower's outer ring." + "\n" + "Their royal majesties thank thee!");

    }

    public void sendMessage(View view){
        View myLayout = findViewById(R.id.quest_part2_style);

        EditText editText = (EditText) myLayout.findViewById(R.id.editText2);
        String message = editText.getText().toString();
        Double paintArea = Double.parseDouble(message); //should add a fail safe (in case the user enters a non number)

        //if the answer is correct, the quest is considered completed and we return to the BeginQuest page
        if (paintArea == correctAns){
            Intent intent = new Intent(this, BeginQuest.class);
            editor.putBoolean(QUEST2_KEY, true);
            editor.commit();
            startActivity(intent);
        }
        else {
            final TextView textView = myLayout.findViewById(R.id.textViewQ2);
            textView.setText("The royal mathematician disputes your answer. Try again!");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Please enter the area to be painted, in square feet and rounded to one decimal place, in the form below. Remember, you've been hired to repaint the flower's outer ring." + "\n" + "Their royal majesties thank thee!");
                }
            }, 5000);
        }

    }


}
