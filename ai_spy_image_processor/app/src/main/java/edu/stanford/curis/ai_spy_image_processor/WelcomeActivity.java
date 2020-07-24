package edu.stanford.curis.ai_spy_image_processor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
