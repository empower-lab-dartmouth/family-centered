package edu.stanford.curis.ai_spy_image_processor;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * In WelcomeActivity, the user chooses which mode of AISpy to play:
 * 1. Computer gives i-sy clues and user guesses (playWithComputerSpy)
 * 2. User gives i-spy clues and computer guesses (playWithChildSpy)
 */
public class WelcomeActivity extends ConversationActivity {
//    private TextToSpeech voice;
    private View view;
    private final String WELCOME_MSG = "Who would you like to do the spying?";
    private final int WHO_TO_SPY_INPUT_REQUEST = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setUpAIVoice(WELCOME_MSG);
        setContentView(R.layout.welcome);
    }

    public void getSpeechInput(View view){
        super.startSpeechRecognition(WHO_TO_SPY_INPUT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case WHO_TO_SPY_INPUT_REQUEST:
                if (resultCode == RESULT_OK && data != null){

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String input = result.get(0);
                    if (input.contains("child") || input.contains("me") || input.contains("I")) {
                        playWithChildSpy(view);
                    } else if (input.contains("computer") || input.contains("you")) {
                        playWithComputerSpy(view);
                    } else {
                        voice.speak("Sorry I didn't catch that. Who would you like to be the spy?", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
        }
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
