package edu.stanford.curis.ai_spy_image_processor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * In PlayWithChildSpyActivity, the computer guesses the chosen i spy object based off of clues given by the child
 */

public class PlayWithChildSpyActivity extends AppCompatActivity {
    public CountDownLatch speakLatch;

    private final HashSet<String> commonColors;
    {
        commonColors = new HashSet<>();
        commonColors.add("white");
        commonColors.add("black");
        commonColors.add("grey");
        commonColors.add("red");
        commonColors.add("green");
        commonColors.add("blue");
        commonColors.add("yellow");
        commonColors.add("orange");
        commonColors.add("pink");
        commonColors.add("brown");
        commonColors.add("purple");
    }

    private final HashSet<String> commonRelativeLocations;
    {
        commonRelativeLocations = new HashSet<>();
        commonRelativeLocations.add("right");
        commonRelativeLocations.add("left");
        commonRelativeLocations.add("above");
        commonRelativeLocations.add("below");
    }

    //Views
    TextView guessView;
    EditText iSpyClueView;
    TextView remainingGuessesView;
    TextView resultView;
    TextView computerRemarkView;

    //constants
    private final int NUM_GUESSES_ALLOWED = 5;
    private final int COLOR_CLUE = 1;
    private final int LOCATION_CLUE = 2;
    private final int GENERAL_KNOWLEDGE_CLUE = 3;

    //String constants
    private final String COMPUTER_INIT = "Great, you do the spying";
    private final String[] COMPUTER_REMARKS = new String[]{"Okay, let me guess. ", "Dang! ", "Oh no! ", "Let me try again. "};
    private final String COMPUTER_OUT_OF_GUESSES = "Well, I'm not out of guesses, but I can't think of anything else. You win!";
    private final String COMPUTER_LOST_REMARK = "I give up. Great job! One Point for you. What is it?";
    private final String COMPUTER_WON_REMARK = "I did it! One point for me.";
    private final String COMPUTER_GUESS = "Is it the ";

    //Instance variables
    private AISpyImage aiSpyImage;
    private HashMap<AISpyObject, Features> iSpyMap;
    private String iSpyClue;
    private AISpyObject computerGuess;
    private TextToSpeech voice;
    private int numGuesses;
    private int numDesperateGuesses;
    private int numGuessesForCurrentObject;
    private int clueType;
    private HashSet<AISpyObject> alreadyGuessedObjects;
    private String[] clueEssentials;
    private boolean desperateMode;

    private final int CLUE_INPUT_REQUEST = 11;
    private final int FEEDBACK_INPUT_REQUEST = 12;



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
        setContentView(R.layout.child_spy);
        setUpAIVoice();

        this.aiSpyImage = AISpyImage.getInstance();
        this.clueType = 0;
        this.iSpyMap = aiSpyImage.getiSpyMap();
        this.speakLatch = new CountDownLatch(0);

        //Set views
        guessView = findViewById(R.id.computerGuess);
        iSpyClueView = findViewById(R.id.iSpyClue);
        remainingGuessesView = findViewById(R.id.remainingGuesses);
        resultView = findViewById(R.id.result);
        computerRemarkView = findViewById(R.id.computerRemark);

        reset();

        //Set image
        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapAPI.getCorrectOrientation(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
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

    /***** Methods for computer guessing *****/

    private void determineClueType(){
        iSpyClue = iSpyClueView.getText().toString();
        String args[] = new String[1];
        Context thisContext = this.getApplicationContext();


        for (String relativeLocation: commonRelativeLocations){ //Check if clue is relative location clue
            if (iSpyClue.contains(relativeLocation)){
                clueType = LOCATION_CLUE;
                getClueEssentials(relativeLocation);
                return;
            }
        }

        for (String color : commonColors){ //Check if clue is a color clue
            if (iSpyClue.contains(color)){
                clueType = COLOR_CLUE;
                getClueEssentials(color);
                return;
            }
        }
        //TODO: Check if general knowledge clue type
    }

    private void getClueEssentials(String primary){

        if (clueType == COLOR_CLUE){
            clueEssentials[0] = primary;
        } else if (clueType == LOCATION_CLUE){
            clueEssentials[0] = primary;
            clueEssentials[1] = iSpyClue;
            //TODO: Put noun phrase of iSpyClue in clueEssentials[1]
        } else if (clueType == GENERAL_KNOWLEDGE_CLUE){
            //TODO
            return;
        }
        else{
            return;
        }
    }

    private String makeGuess(){
        String guess = "";

        if (desperateMode){ //Make desperate guess if can't find object
            guess = getDesperateGuess();
        }
        else if(numGuessesForCurrentObject != 0 && numGuessesForCurrentObject < 3 && numGuessesForCurrentObject < computerGuess.getPossibleLabels().size()){ //Guess another label for current object if have only made 1 other guess for that object
            guess = computerGuess.getPossibleLabels().get(numGuessesForCurrentObject);
            numGuessesForCurrentObject++;
        } else { //Try finding a new object
            numGuessesForCurrentObject = 0;
            switch (clueType){
                case COLOR_CLUE:
                    computerGuess = findObjectFromColor();
                    break;
                case LOCATION_CLUE:
                    computerGuess = findObjectFromLocation();
                    break;
                case GENERAL_KNOWLEDGE_CLUE:
                    computerGuess = findObjectFromGeneralKnowledge(); //TODO
                    break;
            }

            if (computerGuess == null){
                desperateMode = true;
                guess = makeGuess();
            } else {
                guess = computerGuess.getPossibleLabels().get(numGuessesForCurrentObject);
                alreadyGuessedObjects.add(computerGuess);
                numGuessesForCurrentObject++;
            }
        }

        return guess;
    }

    private String getDesperateGuess(){
        if (this.numDesperateGuesses < aiSpyImage.getAllLabels().size()){
            String guess = aiSpyImage.getAllLabels().get(this.numDesperateGuesses).getText();
            numDesperateGuesses++;
            return guess;
        } else {
            return null;
        }

    }

    private AISpyObject findObjectFromColor(){
        String colorClue = clueEssentials[0];
        for (AISpyObject object: iSpyMap.keySet()){
            if (alreadyGuessedObjects.contains(object)) continue;
            if (colorClue.equals(iSpyMap.get(object).color)){
                return object;
            }
        }
        return null;
    }

    private AISpyObject findObjectFromLocation(){

        String direction = clueEssentials[0];
        String wholeClue = clueEssentials[1];

        for (AISpyObject object: iSpyMap.keySet()){
            if (alreadyGuessedObjects.contains(object)) continue;
            HashMap<String, HashSet<AISpyObject>> locationsMao = iSpyMap.get(object).locations;
            if (locationsMao.containsKey(direction)){
                for (AISpyObject possibleRelativeObject: locationsMao.get(direction)){ //TODO: make faster by having locationsMap map to a hash set string of labels instead of objects
                    for (String label : possibleRelativeObject.getPossibleLabels()){
                        if (wholeClue.contains(label.toLowerCase())){
                            return object;
                        }
                    }
                }
            }
        }
        return null;
    }

    private AISpyObject findObjectFromGeneralKnowledge(){
        //TODO
        return null;
    }



    /****** Public listener Methods ********/

    public void startComputerGuessing(View view) {
        makeRemark();
        determineClueType();
        String guess = makeGuess();
        if (guess != null) {
            guessView.setText(guess);
            voice.speak(COMPUTER_GUESS + guess, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            resultView.setText(COMPUTER_LOST_REMARK);
            voice.speak(COMPUTER_LOST_REMARK, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        guessView.setText(guess);
    }

    public void playAgain(View view) {
        reset();
    }

    public void handleCorrectGuess(View view) {
        handleCorrectGuess();
    }
    private void handleCorrectGuess(){
        resultView.setText(COMPUTER_WON_REMARK);
        voice.speak(COMPUTER_WON_REMARK, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void handleIncorrectGuess(View view) throws InterruptedException {
        handleIncorrectGuess();
    }

    private void handleIncorrectGuess(){
        if (numGuesses == 5) return;
        String toSay = "";
        this.numGuesses++;
        if (numGuesses != NUM_GUESSES_ALLOWED){
            computerRemarkView.setText(COMPUTER_REMARKS[numGuesses % COMPUTER_REMARKS.length]);
            toSay += COMPUTER_REMARKS[numGuesses % COMPUTER_REMARKS.length];

            updateRemainingGuesses();
            String guess = makeGuess();
            if (guess != null) {
                guessView.setText(guess);
                toSay += (COMPUTER_GUESS + guess);
            }
            else {

                resultView.setText(COMPUTER_OUT_OF_GUESSES);
                toSay += (COMPUTER_OUT_OF_GUESSES);
            }
        } else {

            resultView.setText(COMPUTER_LOST_REMARK);
            toSay += (COMPUTER_LOST_REMARK);
        }

        voice.speak(toSay, TextToSpeech.QUEUE_FLUSH, null, null);

    }

    /****** Other Helper Methods *******/

    private void reset(){
        this.numGuesses = 0;
        this.numDesperateGuesses = 0;
        this.numGuessesForCurrentObject = 0;
        this.alreadyGuessedObjects = new HashSet<>();
        this.clueEssentials = new String[2];
        this.desperateMode = false;

        guessView.setText("");
        iSpyClueView.setText("");
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_ALLOWED - numGuesses));
        resultView.setText("");
        computerRemarkView.setText("");

    }

    private void updateRemainingGuesses(){
        remainingGuessesView = findViewById(R.id.remainingGuesses);
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_ALLOWED - numGuesses));
    }


    private void makeRemark() {
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses % COMPUTER_REMARKS.length]);
        voice.speak(COMPUTER_REMARKS[numGuesses % COMPUTER_REMARKS.length], TextToSpeech.QUEUE_FLUSH, null, null);
    }

    
    /***** Methods for speech recognition *******/

    public void getSpeechClueInput(View view){
        startSpeechRecognition(CLUE_INPUT_REQUEST);
    }

    public void getSpeechFeedbackInput(View view) {
        startSpeechRecognition(FEEDBACK_INPUT_REQUEST);
    }

    private void startSpeechRecognition(int request){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, request);
        } else {
            Toast.makeText(this, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("*************" + requestCode);

        switch(requestCode){
            case CLUE_INPUT_REQUEST:
                if (resultCode == RESULT_OK && data != null){

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    iSpyClueView = findViewById(R.id.iSpyClue);
                    String guess = result.get(0).toLowerCase();
                    iSpyClueView.setText(guess);
                    startComputerGuessing(findViewById(R.id.startComputerGuessingButton));
                }
                break;
            case FEEDBACK_INPUT_REQUEST:
                if (resultCode == RESULT_OK && data != null){

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String feedback = result.get(0).toLowerCase();
                    if (feedback.contains("yes")){
                        handleCorrectGuess();
                    } else if (feedback.contains("no")){
                        handleIncorrectGuess();
                    }
                }

        }
    }


}

