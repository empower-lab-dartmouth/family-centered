package edu.stanford.curis.ai_spy_image_processor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;


/**
 * In PlayWithComputerSpyActivity, the child guesses the chosen i-spy object based off of clues given by the computer.
 * The child can choose between either color clues or location clues
 */
public class PlayWithComputerSpyActivity extends BasicFunctionality {

    //Views
    private TextView guessView;
    private TextView iSpyClueView;
    private TextView remainingGuessesView;
    private TextView resultView;
    private TextView computerRemarkView;

    //constants
    private final int NUM_GUESSES_ALLOWED = 5;

    private AISpyImage aiSpyImage;
    private String iSpyClue;
    private AISpyObject chosenObject;
    private TextToSpeech voice;
    private int numGuesses;

    //String constants
    private final String COMPUTER_INIT = "Great, I'll do the spying";
    private final String[] COMPUTER_REMARKS = new String[]{"Can you guess what it is?", "Sorry, try again", "That's still not right, sorry. Try again!", "I'm thinking of something else, try again!", "Wanna give up?"};
    private final String MOTIVATION = "You can do it!";
    private final String COMPUTER_WINS = "Gotcha! One point for me. It's the ";
    private final String CHILD_CORRECT_FIRST_TRY = "Wow, you're right on the first try! One point for you";
    private final String CHILD_CORRECT = "You got it right! One point for you.";
    private final String ISPY_PRELUDE = "I spy something that ";

    private final int COLOR_CLUE = 1;
    private final int LOCATION_CLUE = 2;
    private final int GENERAL_KNOWLEDGE_CLUE = 3;


    @Override
    protected void onDestroy() {
        if (voice != null){
            voice.stop();
            voice.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.computer_spy);

        //Set views
        resultView = findViewById(R.id.result);
        guessView = findViewById(R.id.guess);
        iSpyClueView = findViewById(R.id.iSpyClue);
        remainingGuessesView = findViewById(R.id.remainingGuesses);
        computerRemarkView = findViewById(R.id.computerRemark);

        this.aiSpyImage = AISpyImage.getInstance();

        setUpAIVoice();
        reset();
        setISpyImage();
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);
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

                    voice.speak(COMPUTER_INIT, TextToSpeech.QUEUE_FLUSH, null, null);
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

    private void reset(){
        this.numGuesses = 0;

        //Clear old views
        resultView.setText("");
        guessView.setText("");
        iSpyClueView.setText("");
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_ALLOWED - numGuesses));
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);
        voice.speak(COMPUTER_REMARKS[numGuesses], TextToSpeech.QUEUE_FLUSH, null, null);


        chosenObject = aiSpyImage.chooseRandomObject();

    }

    private void setISpyImage(){
        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapAPI.getCorrectOrientation(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
    }


    public void checkGuess(View view) {
        String guess = guessView.getText().toString();
        ArrayList<String> possibleAnswers = chosenObject.getPossibleLabels();

        for (String possibleAnswer: possibleAnswers){
            if (guess.toLowerCase().contains(possibleAnswer)){
                handleCorrectGuess();
                return;
            }
        }

        handleIncorrectGuess();
    }

    private void handleCorrectGuess(){
        if (numGuesses == 0){
            resultView.setText(CHILD_CORRECT_FIRST_TRY);
        } else {
            resultView.setText(CHILD_CORRECT);
            voice.speak(CHILD_CORRECT, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void handleIncorrectGuess(){
        this.numGuesses++;
        if (numGuesses < NUM_GUESSES_ALLOWED){
            setUpNextGuess();
        } else {
            resultView.setText(COMPUTER_WINS + chosenObject.getPossibleLabels().get(0));
            voice.speak(COMPUTER_WINS + chosenObject.getPossibleLabels().get(0), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void setUpNextGuess(){
        guessView.setText("");
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_ALLOWED - numGuesses));
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);
        voice.speak(COMPUTER_REMARKS[numGuesses], TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void playAgain(View view) {
        reset();
    }

    private void giveClue(int clueType){
        TextView iSpyClueView = findViewById(R.id.iSpyClue);
        Features features = aiSpyImage.getiSpyMap().get(chosenObject);

        switch(clueType){
            case COLOR_CLUE:
                iSpyClue = "is " + features.color;
                break;
            case LOCATION_CLUE:
                Random rand = new Random();
                int numDirections = features.locations.keySet().size();
                if (numDirections != 0){
                    String direction = features.locations.keySet().toArray(new String[numDirections])[rand.nextInt(numDirections)]; //Get random direction from location features
                    int numObjectsForDirection = features.locations.get(direction).size();
                    AISpyObject object = features.locations.get(direction).toArray(new AISpyObject[numObjectsForDirection])[rand.nextInt(numObjectsForDirection)]; //Get random object from chosen direction
                    int numLabelsForObject = object.getPossibleLabels().size(); //Get random label from chosen object
                    String label = object.getPossibleLabels().get(rand.nextInt(numLabelsForObject));

                    if (direction == "above" || direction == "below"){
                        iSpyClue = "is " + direction + " the " + label.toLowerCase();
                    } else if (direction == "right" || direction == "left"){
                        iSpyClue = "is to the " + direction + " of the " + label.toLowerCase();
                    }
                }
                break;
            case GENERAL_KNOWLEDGE_CLUE:
                iSpyClue = features.wiki;
                break;
        }

        iSpyClueView.setText(iSpyClue);
        voice.speak(ISPY_PRELUDE+ iSpyClue, TextToSpeech.QUEUE_FLUSH, null, null);

    }

    public void giveWikiClue(View view){
        int clueType = GENERAL_KNOWLEDGE_CLUE;
        giveClue(clueType);
    }

    public void giveColorClue(View view) {
        int clueType = COLOR_CLUE;
        giveClue(clueType);
    }

    public void giveLocationClue(View view) {
        int clueType = LOCATION_CLUE;
        giveClue(clueType);

    }
    public void getChoiceInput(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 9);
        } else {
            Toast.makeText(this, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
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
                    EditText guessView = findViewById(R.id.guess);
                    guessView.setText(result.get(0));
                }
            case 9:
                if (resultCode == RESULT_OK && data != null){

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String input = result.get(0);
                    System.out.println("*********************************" + result.get(0));
                    if (input.contains("color") || input.contains("colors")) {
                        giveColorClue(findViewById(R.id.iSpyClue));
                    } else if (input.contains("location") || input.contains("locations")) {
                        giveLocationClue(findViewById(R.id.iSpyClue));
                    } else if (input.contains("Wiki") || input.contains("Wikipedia")){
                        giveWikiClue(findViewById(R.id.iSpyClue));
                    }
                }
        }
    }


}
