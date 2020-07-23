package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;

public class PlayWithChildSpyActivity extends BasicFunctionality {

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
    private final String[] COMPUTER_REMARKS = new String[]{"Okay, let me guess", "Dang!", "Oh no!", "Let me try again"};
    private final String COMPUTER_LOST_REMARK = "I give up. Great job! One Point for you. What is it?";
    private final String COMPUTER_WON_REMARK = "I did it! One point for me.";

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_spy);

        this.aiSpyImage = AISpyImage.getInstance();
        this.iSpyMap = aiSpyImage.getiSpyMap();
        this.numGuesses = 0;
        this.numDesperateGuesses = 0;
        this.numGuessesForCurrentObject = 0;
        this.clueType = 0;
        this.alreadyGuessedObjects = new HashSet<>();
        this.clueEssentials = new String[2];
        this.desperateMode = false;

        //Set views
        guessView = findViewById(R.id.computerGuess);
        guessView.setText("");
        iSpyClueView = findViewById(R.id.iSpyClue);
        iSpyClueView.setText("");
        remainingGuessesView = findViewById(R.id.remainingGuesses);
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_ALLOWED - numGuesses));
        resultView = findViewById(R.id.result);
        resultView.setText("");
        computerRemarkView = findViewById(R.id.computerRemark);

        //Set image
        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapFactory.decodeFile(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
    }

    /***** Methods for computer guessing *****/

    private void determineClueType(){
        iSpyClue = iSpyClueView.getText().toString();

        for (String color : commonColors){ //Check if clue is a color clue
            if (iSpyClue.contains(color)){
                clueType = COLOR_CLUE;
                getClueEssentials(color);
                return;
            }
        }

        for (String relativeLocation: commonRelativeLocations){ //Check if clue is relative location clue
            if (iSpyClue.contains(relativeLocation)){
                clueType = LOCATION_CLUE;
                getClueEssentials(relativeLocation);
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
        else if(numGuessesForCurrentObject == 1){ //Guess another label for current object if have only made 1 other guess for that object
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
        String guess = aiSpyImage.getAllLabels().get(this.numDesperateGuesses).getText();
        numDesperateGuesses++;
        return guess;
    }

    private AISpyObject findObjectFromColor(){
        String colorClue = clueEssentials[0];
        for (AISpyObject object: iSpyMap.keySet()){
            if (alreadyGuessedObjects.contains(object)) continue;
            if (colorClue == iSpyMap.get(object).color){
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
        guessView.setText(makeGuess());
    }

    public void getSpeechInput(View view) {
    }

    public void playAgain(View view) {
        reset();
    }

    public void handleCorrectGuess(View view) {
        resultView.setText(COMPUTER_WON_REMARK);
    }

    public void handleIncorrectGuess(View view) {
        this.numGuesses++;
        if (numGuesses != NUM_GUESSES_ALLOWED){
            makeRemark();
            updateRemainingGuesses();
            guessView.setText(makeGuess());
        } else {
            resultView.setText(COMPUTER_LOST_REMARK);
        }
    }








    private void reset(){
        this.numGuesses = 0;
        this.numDesperateGuesses = 0;
        this.numGuessesForCurrentObject = 0;
        this.alreadyGuessedObjects.clear();

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
    }
}

