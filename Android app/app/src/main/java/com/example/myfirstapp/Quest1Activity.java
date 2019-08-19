package com.example.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//9

//instructs user about first quest. You may want to add scrolling functionality (similar to IntroSequenceActivity2).
public class Quest1Activity extends BasicFunctionalityPart2 {
    public int currentStep = -1;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest1);

        sendMessageInner();
    }

    public void sendMessage(View view){
        sendMessageInner();
    }

    //handles display sequence
    public void sendMessageInner(){
        currentStep++;
        if (currentStep == 0) displayChange0();
        if (currentStep == 1) displayChange1();
        if (currentStep == 2) displayChange2();
        if (currentStep == 3) displayChange3();
        if (currentStep == 4) displayChange4();
        if (currentStep == 5) {
            Intent intent = new Intent(this, Quest1Part2Activity.class);
            startActivity(intent);
        }
    }

    void displayChange0(){
        View myLayout = findViewById(R.id.quest1_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ);
        textView.setText("Today, you and " + pref.getString(DRAGON_NAME_KEY, DRAGON_NAME_DEFAULT) + " have been given a special task: to remake the magical Kingdom of Quadria (known to humans as the Stanford Quad).");
    }

    void displayChange1(){
        View myLayout = findViewById(R.id.quest1_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ);
        textView.setText("Your first task has to do with the flower of fortune, located in the middle of the quad. Click 'Continue' once you've found it." );
    }

    void displayChange2(){
        View myLayout = findViewById(R.id.quest1_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ);
        textView.setText("The queen and king of Quadria think the flower of fortune could use a new look. They'd like you to paint the outer ring of the flower an intriguing color known as periwinkle blue.");
    }

    void displayChange3(){
        View myLayout = findViewById(R.id.quest1_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ);
        textView.setText("Periwinkle blue paint is hard to find, so their majesties want to make sure they buy the exact right amount.");
    }

    void displayChange4(){
        View myLayout = findViewById(R.id.quest1_style);
        TextView textView = myLayout.findViewById(R.id.textViewQ);
        textView.setText("Will you help them figure out what area they need to paint?");
        Button button = myLayout.findViewById(R.id.buttonQ);
        button.setText("Yes");
    }


}
