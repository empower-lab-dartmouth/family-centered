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
public class WelcomeActivity extends AppCompatActivity {
    private TextToSpeech voice;
    private View view;
    private final String WELCOME_MSG = "Who would you like to do the spying?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpAIVoice();
        setContentView(R.layout.welcome);
    }

    private void setUpAIVoice(){
        voice = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = voice.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported");
                    } else {

                    }

                    voice.speak(WELCOME_MSG, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        }, "com.google.android.tts");

        //https://stackoverflow.com/questions/9815245/android-text-to-speech-male-voice
        Set<String> a=new HashSet<>();
        a.add("male");//here you can give male if you want to select male voice.
        Voice v=new Voice("en-us-x-sfg#male_2-local",new Locale("en","US"),400,200,true,a);
        voice.setVoice(v);
        voice.setSpeechRate(0.8f);
    }
    //https://www.youtube.com/watch?v=0bLwXw5aFOs
    public void getSpeechInput(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("*************" + requestCode);

        switch(requestCode){
            case 10:
                if (resultCode == RESULT_OK && data != null){

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String input = result.get(0);
                    System.out.println("*********************************" + result.get(0));
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
