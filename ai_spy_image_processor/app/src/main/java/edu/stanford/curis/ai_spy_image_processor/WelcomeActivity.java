package edu.stanford.curis.ai_spy_image_processor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * In WelcomeActivity, the user chooses which mode of AISpy to play:
 * 1. Computer gives i-sy clues and user guesses (playWithComputerSpy)
 * 2. User gives i-spy clues and computer guesses (playWithChildSpy)
 */
public class WelcomeActivity extends BasicFunctionality {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
    }


    public void playWithComputerSpy(View view) {
        Intent intent = new Intent(this, PlayWithComputerSpyActivity.class);
        startActivity(intent);
    }

    public void playWithChildSpy(View view) {
        Intent intent = new Intent(this, PlayWithChildSpyActivity.class);
        startActivity(intent);
    }
}
