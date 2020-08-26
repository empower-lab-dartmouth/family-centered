package projects.android.aispy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

/**
 * In PlayWithChildSpyActivity, the computer guesses the chosen i spy object based off of clues given by the child
 */

public class PlayWithChildSpyActivity extends ConversationActivity {
    public CountDownLatch speakLatch;

    //Used as key words to detect if a clue is a color clue
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

    //Used as key words to detect if a clue is a location clue
    private final HashMap<String,HashSet<String>> commonRelativeLocations;
    {
        commonRelativeLocations = new HashMap<>();
        HashSet<String> commonIndicatorsOfRight = new HashSet<>();
        commonIndicatorsOfRight.add("right");
        commonRelativeLocations.put("right", commonIndicatorsOfRight);

        HashSet<String> commonIndicatorsOfLeft = new HashSet<>();
        commonIndicatorsOfRight.add("left");
        commonRelativeLocations.put("left", commonIndicatorsOfLeft);

        HashSet<String> commonIndicatorsOfAbove = new HashSet<>();
        commonIndicatorsOfAbove.add("above");
        commonIndicatorsOfAbove.add("over");
        commonIndicatorsOfAbove.add("top");
        commonRelativeLocations.put("above", commonIndicatorsOfAbove);

        HashSet<String> commonIndicatorsOfBelow = new HashSet<>();
        commonIndicatorsOfBelow.add("below");
        commonIndicatorsOfBelow.add("under");
        commonIndicatorsOfBelow.add("top");
        commonRelativeLocations.put("below", commonIndicatorsOfBelow);
    }

    //constants
    private final int NUM_GUESSES_ALLOWED = 5;
    private final int MAX_GUESSES_FOR_ONE_OBJECT = 3;
    private final int COLOR_CLUE = 1;
    private final int LOCATION_CLUE = 2;

    //String constants
    private final String COMPUTER_INIT = "Great, you do the spying";
    private final String[] COMPUTER_REMARKS = new String[]{"Okay, let me guess. ", "Dang! ", "Oh no! ", "Let me try again. "};
    private final String COMPUTER_OUT_OF_GUESSES = "Well, I'm not out of guesses, but I can't think of anything else. You win!";
    private final String COMPUTER_LOST_REMARK = "I give up. Great job! One Point for you. What is it?";
    private final String COMPUTER_WON_REMARK = "I did it! One point for me.";
    private final String COMPUTER_GUESS = "Is it the ";
    private final String PLAY_AGAIN_PROMPT_A = "Do you want to play again with a new image? Or do you want to use the same image?";
    private final String PLAY_AGAIN_PROMPT_B = "Okay, I can't see anything else in this image, so let's choose a new one";

    //Instance variables
    private AISpyImage aiSpyImage;
    private HashMap<AISpyObject, Features> iSpyMap;
    private String iSpyClue;
    private AISpyObject computerGuess;
    private int numGuesses;
    private int numDesperateGuesses;
    private int numGuessesForCurrentObject;
    private int clueType;
    private HashSet<AISpyObject> alreadyGuessedObjects;
    private String[] clueEssentials;
    private boolean desperateMode;
    private boolean hasGivenClue;
    private boolean playAgainRequestInProgress;
    private ArrayList<AISpyObject> objectPool;

    private final int CLUE_INPUT_REQUEST = 11;
    private final int FEEDBACK_INPUT_REQUEST = 12;
    private final int PLAY_AGAIN_REQUEST = 20;

    /**
     * Initializes and resets all views and instance variables
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_spy);
        super.setUpAIVoice(COMPUTER_INIT);

        this.aiSpyImage = AISpyImage.getInstance();
        this.clueType = 0;
        this.iSpyMap = aiSpyImage.getiSpyMap();
        this.speakLatch = new CountDownLatch(0);

        objectPool = aiSpyImage.getAllObjects();

        reset();

        //Set image
        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapAPI.getCorrectOrientation(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
    }

    /***** Methods for computer guessing *****/

    /**
     * Searches the given clue for key words in order to determine the clue type
     * PlayWithChildSpyActivity currently is only able to detect location and color clues.
     * If the clue is neither, the computer will guess in desperateMode
     */
    private void determineClueType(){
        String args[] = new String[1];

        //Check if clue is relative location clue
        for (String relativeLocation: commonRelativeLocations.keySet()){
            for (String commonIndicator: commonRelativeLocations.get(relativeLocation)) {
                if (iSpyClue.contains(commonIndicator)) {
                    clueType = LOCATION_CLUE;
                    getClueEssentials(relativeLocation);
                    return;
                }
            }
        }

        //Check if clue is a color clue
        for (String color : commonColors){
            if (iSpyClue.contains(color)){
                clueType = COLOR_CLUE;
                getClueEssentials(color);
                return;
            }
        }
    }

    /**
     * Fills clueEssentials with important clue information. clueEssentials[0] stores the primary information (the color or the relative direction)
     * clueEssentials[1] stores the entire clue given
     */
    private void getClueEssentials(String primary){
        if (clueType == COLOR_CLUE){
            clueEssentials[0] = primary;
        } else if (clueType == LOCATION_CLUE){
            clueEssentials[0] = primary;
            clueEssentials[1] = iSpyClue;
        }
        else{
            return;
        }
    }

    /**
     * Returns the computers next guess. First tries to find an AISpyObject that matches the clue given. Will make up to MAX_GUESSES_FOR_ONE_OBJECT label guesses
     * based off of a found matching AISpyObject. If can't find a matching AISpyObject, will go to desperateMode and guess labels from the top of allLabels that were detected
     */
    private String makeGuess(){
        String guess = "";

        if (desperateMode){ //Make desperate guess if can't find object
            guess = getDesperateGuess();
        }
        else if(canMakeAnotherGuessForCurrentObject()){ //Guess another label for current object if have only made 1 other guess for that object
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
            }

            if (computerGuess == null){ //If couldn't find a possible object based on the clue, go to desperate mode
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

    /**
     * Returns true if it is not the first guess for the currentObject that has been found, but is also less than MAX_GUESSES_FOR_ONE_OBJECT
     * @return
     */
    private boolean canMakeAnotherGuessForCurrentObject(){
        return numGuessesForCurrentObject != 0 && numGuessesForCurrentObject < MAX_GUESSES_FOR_ONE_OBJECT && numGuessesForCurrentObject < computerGuess.getPossibleLabels().size();
    }

    /**
     * Only called when in desperateMode. Returns a label from allLabels that have been detected by the computer. The label returned is located at the
     * index of this.numDesperateGuesses
     */
    private String getDesperateGuess(){
        if (this.numDesperateGuesses < aiSpyImage.getAllLabels().size()){
            String guess = aiSpyImage.getAllLabels().get(this.numDesperateGuesses).getText();
            numDesperateGuesses++;
            return guess;
        } else {
            return null;
        }
    }

    /**
     * Returns a detected AISpyObject that has a matching color to the given clue. Skips any AISpyObject that has already been guessed.
     * Returns null if there are no matching AISpyObjects
     */
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

    /**
     * Returns a detected AISpyObject that has a matching relative location to the given clue. Skips any AISpyObject that has already been guessed.
     * Returns null if there are no matching AISpyObjects
     */
    private AISpyObject findObjectFromLocation(){

        String direction = clueEssentials[0];
        String wholeClue = clueEssentials[1];

        for (AISpyObject object: iSpyMap.keySet()){
            if (alreadyGuessedObjects.contains(object)) continue;
            HashMap<String, HashSet<AISpyObject>> locationsMao = iSpyMap.get(object).locations;
            if (locationsMao.containsKey(direction)){
                for (AISpyObject possibleRelativeObject: locationsMao.get(direction)){
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

    /****** Public listener Methods ********/

    /**
     * Computer makes the first guess after determining clue type.
     */
    public void startComputerGuessing() {
        makeRemark();
        determineClueType();
        String guess = makeGuess();
        TextView guessText = findViewById(R.id.guess);
        if (guess != null) {
            guessText.setText(COMPUTER_GUESS + guess + "?");
            voice.speak(COMPUTER_GUESS + guess, TextToSpeech.QUEUE_FLUSH, null, COMPUTER_GUESS);
        }
        else {
            guessText.setText(COMPUTER_LOST_REMARK);
            voice.speak(COMPUTER_LOST_REMARK, TextToSpeech.QUEUE_FLUSH, null, COMPUTER_LOST_REMARK);
        }
    }

    public void playAgain(View view) {
        reset();
        playAgain();
    }

    /**
     * Resets all aspects of the ispy game PlayWithChildSpyActivity except for the picture
     */
    private void reset(){
        this.numGuesses = 0;
        this.numDesperateGuesses = 0;
        this.numGuessesForCurrentObject = 0;
        this.alreadyGuessedObjects = new HashSet<>();
        this.clueEssentials = new String[2];
        this.desperateMode = false;
        this.hasGivenClue = false;
        this.playAgainRequestInProgress = false;
    }

    /**
     * If there are still objects in objectPool, prompts you to choose between playing again with the same image or a new image
     * prompts you to play again by returning to the home screen and choosing a new image
     */
    private void playAgain(){
        TextView guessText = findViewById(R.id.guess);
        if (this.objectPool.size() != 0){
            guessText.setText(PLAY_AGAIN_PROMPT_A);
            voice.speak(PLAY_AGAIN_PROMPT_A, TextToSpeech.QUEUE_FLUSH, null, PLAY_AGAIN_PROMPT_A);
            playAgainRequestInProgress = true;

        } else { //Go back to first screen and choose a new image
            guessText.setText(PLAY_AGAIN_PROMPT_B);
            voice.speak(PLAY_AGAIN_PROMPT_B, TextToSpeech.QUEUE_FLUSH, null, PLAY_AGAIN_PROMPT_B);
            playAgainNewImage();
        }
    }

    /**
     * Resets the screen and instance variables to play again with the same image
     */
    private void playAgainSameImage(){
        reset();
        TextView guessText = findViewById(R.id.guess);
        guessText.setText(COMPUTER_INIT);
        voice.speak(COMPUTER_INIT, TextToSpeech.QUEUE_FLUSH, null, COMPUTER_INIT);
    }

    /**
     * Returns user to the main screen to choose an image
     */
    private void playAgainNewImage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Prompts computer to speak COMPUTER_WON_REMARK
     */
    private void handleCorrectGuess(){
        objectPool.remove(computerGuess);
        TextView guessText = findViewById(R.id.guess);
        guessText.setText(COMPUTER_WON_REMARK);
        voice.speak(COMPUTER_WON_REMARK, TextToSpeech.QUEUE_FLUSH, null, COMPUTER_WON_REMARK);
    }
    public void handleCorrectGuess(View view) {
        handleCorrectGuess();
    }

    /**
     * Does nothing if user has already reached max NUM_GUESSES_ALLOWED. If this incorrect guess puts the computer
     * at max NUM_GUESSES_ALLOWED, prompts the computer to speak COMPUTER_LOST_REMARK. Otherwise, if the computer
     * still has guesses remaining, the computer makes another guess
     */
    private void handleIncorrectGuess(){
        if (numGuesses == NUM_GUESSES_ALLOWED) return;
        String toSay = "";
        this.numGuesses++;
        if (numGuesses != NUM_GUESSES_ALLOWED){
            toSay += COMPUTER_REMARKS[numGuesses % COMPUTER_REMARKS.length];
            String guess = makeGuess();
            if (guess != null) {
                toSay += (COMPUTER_GUESS + guess);
            }
            else {
                toSay += (COMPUTER_OUT_OF_GUESSES);
            }
        } else {
            toSay += (COMPUTER_LOST_REMARK);
        }
        TextView guessText = findViewById(R.id.guess);
        guessText.setText(toSay + "?");
        voice.speak(toSay, TextToSpeech.QUEUE_FLUSH, null, toSay);

    }
    public void handleIncorrectGuess(View view) throws InterruptedException {
        handleIncorrectGuess();
    }


    /**
     * Prompts the computer to speak a remark depending on which guess the computer is on.
     */
    private void makeRemark() {
        TextView guessText = findViewById(R.id.guess);
        guessText.setText(COMPUTER_REMARKS[numGuesses % COMPUTER_REMARKS.length]);
        voice.speak(COMPUTER_REMARKS[numGuesses % COMPUTER_REMARKS.length], TextToSpeech.QUEUE_FLUSH, null, COMPUTER_REMARKS[numGuesses % COMPUTER_REMARKS.length]);
    }

    
    /***** Methods for speech recognition *******/

    public void getSpeechInput(View view) {
        if (playAgainRequestInProgress){
            super.startSpeechRecognition(PLAY_AGAIN_REQUEST);
        } else if (!hasGivenClue){
            super.startSpeechRecognition(CLUE_INPUT_REQUEST);
        } else {
            super.startSpeechRecognition(FEEDBACK_INPUT_REQUEST);
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
                    String guess = result.get(0).toLowerCase();
                    iSpyClue = guess;
                    startComputerGuessing();
                    hasGivenClue = true;
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
                break;
            case PLAY_AGAIN_REQUEST:
                if (resultCode == RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String response = result.get(0);
                    if (response.contains("new")){
                        playAgainNewImage();
                    } else if (response.contains("same")){
                        playAgainSameImage();
                    }
                }
                break;
        }
    }
}

