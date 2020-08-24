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
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;


/**
 * In PlayWithComputerSpyActivity, the child guesses the chosen i-spy object based off of clues given by the computer.
 * The child can choose between either color clues or location clues
 */
public class PlayWithComputerSpyActivity extends ConversationActivity {

    //Views
    private TextView guessView;
    private TextView iSpyClueView;
    private TextView remainingGuessesView;
    private TextView resultView;
    private TextView computerRemarkView;

    //Constants
    private final int NUM_GUESSES_ALLOWED = 5;

    //Instance variables
    private AISpyImage aiSpyImage;
    private String iSpyClue;
    private AISpyObject chosenObject;
    private int numGuesses;

    //String constants
    private final String COMPUTER_INIT = "Great, I'll do the spying";
    private final String[] COMPUTER_REMARKS = new String[]{"Can you guess what it is?", "Sorry, try again", "That's still not right, sorry. Try again!", "I'm thinking of something else, try again!", "Wanna give up?"};
    private final String MOTIVATION = "You can do it!";
    private final String COMPUTER_WINS = "Gotcha! One point for me. It's the ";
    private final String CHILD_CORRECT_FIRST_TRY = "Wow, you're right on the first try! One point for you";
    private final String CHILD_CORRECT = "You got it right! One point for you.";
    private final String ISPY_PRELUDE = "I spy something that ";

    private final String COLOR_CLUE = "COLOR";
    private final String LOCATION_CLUE = "LOCATION";
    private final String WIKI_CLUE = "WIKI";
    private final String CONCEPTNET_CLUE= "CONCEPTNET";
    private final int GUESS_INPUT_REQUEST = 10;
    public final String TAG = "COMPUTER_SPY";

    private int numCluesGiven;
    private String clueType;
    private HashMap<String, ArrayList<String>> cluePool;

    /**
     * Initializes and resets all views and instance variables
     * @param savedInstanceState
     */
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

        super.setUpAIVoice(COMPUTER_INIT);
        reset();
        setISpyImage();
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);


    }

    /**
     * Resets all aspects of the i-spy game PlayWithComputerSpyActivity except for the picture
     */
    private void reset(){
        this.numGuesses = 0;

        //Clear old views
        resultView.setText("");
        guessView.setText("");
        iSpyClueView.setText("");
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_ALLOWED - numGuesses));
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);
        voice.speak(COMPUTER_REMARKS[numGuesses], TextToSpeech.QUEUE_FLUSH, null, COMPUTER_REMARKS[numGuesses]);


        chosenObject = aiSpyImage.chooseRandomObject();
        Features features = aiSpyImage.getiSpyMap().get(chosenObject);
        cluePool = makeCluePool(features);
        numCluesGiven = 0;
    }

    /**
     * makes string clues for every feature/clue type of a detected object: color, location, wiki, and conceptnet
     * @param features
     * @return a HashMap that maps from the clue type to an ArrayList of possible clues
     */
    private HashMap<String, ArrayList<String>> makeCluePool(Features features){
        HashMap<String, ArrayList<String>> cluePool = new HashMap<>();
        ArrayList<String> colorClue = new ArrayList<>();
        colorClue.add(features.color);
        ArrayList<String> wikiClue = new ArrayList<>();
        wikiClue.add(features.wiki);
        ArrayList<String> locationClues = makeLocationClues(features.locations);
        ArrayList<String> conceptNetClues = makeConceptNetClues(features.conceptNet);

        cluePool.put(COLOR_CLUE, colorClue);
        cluePool.put(WIKI_CLUE, wikiClue);
        if (locationClues.size() != 0) cluePool.put(LOCATION_CLUE, locationClues);
        if (conceptNetClues.size() != 0) cluePool.put(CONCEPTNET_CLUE, conceptNetClues);
        return cluePool;
    }

    /**
     * Converts all relations and endpoints of the concept net to an ArrayList of string clues
     * @param conceptNetMap
     * @return ArrayList of string concept net clues
     */
    private ArrayList<String> makeConceptNetClues(HashMap<String, ArrayList<String>> conceptNetMap){
        ArrayList<String> conceptNetClues = new ArrayList<>();
        for (String relation : conceptNetMap.keySet()){
            for (String endpoint : conceptNetMap.get(relation)){
                String conceptNetClue = ConceptNetAPI.makeConceptNetClue(relation, endpoint);
                conceptNetClues.add(conceptNetClue);
            }
        }
        return conceptNetClues;
    }

    /**
     * Converts all directions and endpoints of location features to an ArrayList of string clues
     * @param locations
     * @return ArrayList of string location clues
     */
    private ArrayList<String> makeLocationClues(HashMap<String, HashSet<AISpyObject>> locations){
        ArrayList<String> locationClues = new ArrayList<>();
        for (String direction : locations.keySet()){
            for (AISpyObject object : locations.get(direction)){
                String locationClue = "";
                String label = object.getPossibleLabels().get(0); //Get the top label
                if (direction == "above" || direction == "below"){
                    locationClue = "is " + direction + " the " + label.toLowerCase();
                } else if (direction == "right" || direction == "left"){
                    locationClue = "is to the " + direction + " of the " + label.toLowerCase();
                }
                locationClues.add(locationClue);
            }
        }
        return locationClues;
    }

    /**
     * Sets an ImageView on the screen with the user-chosen image
     */
    private void setISpyImage(){
        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapAPI.getCorrectOrientation(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
    }

    /**
     * calls the private method checkGuess, passing in the text from the guessView
     * @param view
     */
    public void checkGuess(View view){
        String guess = guessView.getText().toString();
        checkGuess(guess);
    }

    /**
     * Loops through all possible answers to see if the guess is correct. If it is correct, calls handleCorrectGuess()
     * If it is incorrect, calls handleIncorrectGuess()
     * @param guess
     */
    private void checkGuess(String guess) {
        ArrayList<String> possibleAnswers = chosenObject.getPossibleLabels();
        for (String possibleAnswer: possibleAnswers){
            if (guess.toLowerCase().contains(possibleAnswer)){
                handleCorrectGuess();
                return;
            }
        }
        handleIncorrectGuess();
    }

    /**
     * Prompts the computer to speak the appropriate congratulatory message
     */
    private void handleCorrectGuess(){
        if (numGuesses == 0){
            resultView.setText(CHILD_CORRECT_FIRST_TRY);
            voice.speak(CHILD_CORRECT_FIRST_TRY, TextToSpeech.QUEUE_FLUSH, null, CHILD_CORRECT_FIRST_TRY);
        } else {
            resultView.setText(CHILD_CORRECT);
            voice.speak(CHILD_CORRECT, TextToSpeech.QUEUE_FLUSH, null, CHILD_CORRECT);
        }
    }

    /**
     * If there are still guesses remaining, calls setUpNextGuess()
     * Otherwise, prompts the computer to speak the COMPUTER_WINS message
     */
    private void handleIncorrectGuess(){
        this.numGuesses++;
        if (numGuesses < NUM_GUESSES_ALLOWED){
            setUpNextGuess();
        } else {
            resultView.setText(COMPUTER_WINS + chosenObject.getPrimaryLabel());
            voice.speak(COMPUTER_WINS + chosenObject.getPrimaryLabel(), TextToSpeech.QUEUE_FLUSH, null, COMPUTER_WINS);
        }
    }

    /**
     * Resets the guessView and prompts the computer to speak the next remark prompting the user to make another guess
     */
    private void setUpNextGuess(){
        guessView.setText("");
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_ALLOWED - numGuesses));
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);
        voice.speak(COMPUTER_REMARKS[numGuesses], TextToSpeech.QUEUE_FLUSH, null, COMPUTER_REMARKS[numGuesses]);
    }

    public void playAgain(View view) {
        reset();
    }

    /**
     * Randomly chooses a clueType from whatever clue types is passed in. Then randomly selects a clue from that
     * clueType to use for the current iSpyClue. Removes that clue from the cluePool so that there are no repeat clues
     * @param clueType is the clueType to give a clue for
     */
    private void giveClue(String clueType){
        TextView iSpyClueView = findViewById(R.id.iSpyClue);
        Random rand = new Random();
        if(!cluePool.keySet().isEmpty()){

            switch(clueType){
                case COLOR_CLUE:
                    if(cluePool.containsKey(COLOR_CLUE)){
                        iSpyClue = "is " + cluePool.get(COLOR_CLUE).get(0);
                        cluePool.remove(COLOR_CLUE);
                    } else {
                        iSpyClue = "no more color clues";
                    }
                    break;
                case LOCATION_CLUE:
                    if(cluePool.containsKey(LOCATION_CLUE)){
                        ArrayList<String> locations = cluePool.get(LOCATION_CLUE);
                        int i = rand.nextInt(locations.size());
                        iSpyClue = locations.get(i);
                        locations.remove(i);
                        if (locations.size() == 0) cluePool.remove(LOCATION_CLUE);
                    } else {
                        iSpyClue = "no more location clues";
                    }
                    break;
                case WIKI_CLUE:
                    if(cluePool.containsKey(WIKI_CLUE)){
                        iSpyClue = cluePool.get(WIKI_CLUE).get(0);
                        cluePool.remove(WIKI_CLUE);
                    } else {
                        iSpyClue = "no more wiki clues";
                    }
                    break;
                case CONCEPTNET_CLUE:
                    if(cluePool.containsKey(CONCEPTNET_CLUE)){
                        ArrayList<String> conceptNetClues = cluePool.get(CONCEPTNET_CLUE);
                        int i = rand.nextInt(conceptNetClues.size());
                        iSpyClue = conceptNetClues.get(i);
                        conceptNetClues.remove(i);
                        if (conceptNetClues.size() == 0) cluePool.remove(CONCEPTNET_CLUE);
                    } else {
                        iSpyClue = "no more concept net clues";
                    }
                    break;
            }
        } else {
            Log.i(TAG, "out of clues");
            iSpyClue = "out of clues";
        }

        iSpyClueView.setText(iSpyClue);
        numCluesGiven++;

        voice.speak(ISPY_PRELUDE+ iSpyClue, TextToSpeech.QUEUE_FLUSH, null, ISPY_PRELUDE);

    }

    public void giveConceptNetClue(View view){
        String clueType = CONCEPTNET_CLUE;
        giveClue(clueType);
    }

    public void giveWikiClue(View view){
        String clueType = WIKI_CLUE;
        giveClue(clueType);
    }

    public void giveColorClue(View view) {
        String clueType = COLOR_CLUE;
        giveClue(clueType);
    }

    public void giveLocationClue(View view) {
        String clueType = LOCATION_CLUE;
        giveClue(clueType);

    }

    public void getSpeechInput(View view){
        super.startSpeechRecognition(GUESS_INPUT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("*************" + requestCode);

        switch(requestCode){
            case GUESS_INPUT_REQUEST:
                if (resultCode == RESULT_OK && data != null){

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    EditText guessView = findViewById(R.id.guess);
                    String guess = result.get(0);
                    guessView.setText(guess);
                    checkGuess(guess);
                }
        }
    }


}
