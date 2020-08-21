package projects.android.aispy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;


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

    //constants
    private final int NUM_GUESSES_UNTIL_CHECKIN = 3;

    private AISpyImage aiSpyImage;
    private AISpyObject chosenObject;
    private int numGuesses;

    //String constants
    private final String COMPUTER_INIT = "Great, I'll do the spying. ";
    private final String[] COMPUTER_REMARKS = new String[]{"Can you guess what it is?", "Sorry, try again", "That's still not right, sorry. Try again!", "I'm thinking of something else, try again!", "Wanna give up?"};
    private final String MOTIVATION = "You can do it!";
    private final String COMPUTER_WINS = "Gotcha! One point for me. It's the ";
    private final String CHILD_CORRECT_FIRST_TRY = "Wow, you're right on the first try! One point for you";
    private final String CHILD_CORRECT = "You got it right! One point for you.";
    private final String ISPY_PRELUDE = "I spy something that ";
    private final String PLAY_AGAIN_PROMPT_A = "Do you want to play again with a new image? Or do you want to use the same image?";
    private final String PLAY_AGAIN_PROMPT_B = "Okay, I can't see anything else in this image, so let's choose a new one";
    private final String CHECKIN = "That's still not right. Do you want to keep guessing with another clue? Or do you want to give up?";


    private final String COLOR_CLUE = "COLOR";
    private final String LOCATION_CLUE = "LOCATION";
    private final String WIKI_CLUE = "WIKI";
    private final String CONCEPTNET_CLUE= "CONCEPTNET";
    private final int GUESS_INPUT_REQUEST = 10;
    private final int PLAY_AGAIN_REQUEST = 20;
    private final int CHECKIN_REQUEST = 30;
    public final String TAG = "COMPUTER_SPY";

    private int numCluesGiven;
    private String clueType;
    private HashMap<String, ArrayList<String>> cluePool;
    private ArrayList<AISpyObject> objectPool;
    private boolean playAgainRequestInProgress;
    private boolean checkinInProgress;

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
        objectPool = aiSpyImage.getAllObjects();

        setUpPlayForCurrentImage();

        String firstClue = ISPY_PRELUDE + getClue();
        super.setUpAIVoice(COMPUTER_INIT + firstClue + COMPUTER_REMARKS[numGuesses]);
        setISpyImage();
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);


    }

    /**
     * Resets all aspects of the i-spy game PlayWithComputerSpyActivity except for the picture
     */
    private void setUpPlayForCurrentImage(){
        this.numGuesses = 0;

        //Clear old views
        resultView.setText("");
        guessView.setText("");
        iSpyClueView.setText("");
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_UNTIL_CHECKIN - numGuesses));
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);


        chosenObject = chooseRandomObject();
        Features features = aiSpyImage.getiSpyMap().get(chosenObject);
        cluePool = makeCluePool(features);
        numCluesGiven = 0;
        playAgainRequestInProgress = false;
        checkinInProgress = false;
    }

    /**
     * Chooses a random AISpyObject from objectPool, the pool of detected AISpy objects. Deletes it from objectPool so that
     * it can't be chosen a second time
     */
    private AISpyObject chooseRandomObject(){
        Random rand = new Random();
        AISpyObject chosenObject = this.objectPool.get(rand.nextInt(this.objectPool.size()));
        objectPool.remove(chosenObject);
        return chosenObject;
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
            voice.speak(CHILD_CORRECT_FIRST_TRY, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            resultView.setText(CHILD_CORRECT);
            voice.speak(CHILD_CORRECT, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    /**
     * If there are still guesses remaining, calls setUpNextGuess()
     * Otherwise, prompts the computer to speak the COMPUTER_WINS message
     */
    private void handleIncorrectGuess(){
        this.numGuesses++;
        if (numGuesses < NUM_GUESSES_UNTIL_CHECKIN){
            setUpNextGuess();
        } else {
            checkinInProgress = true;
            voice.speak(CHECKIN, TextToSpeech.QUEUE_FLUSH, null, null);
//            resultView.setText(COMPUTER_WINS + chosenObject.getPossibleLabels().get(0));
//            voice.speak(COMPUTER_WINS + chosenObject.getPossibleLabels().get(0), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    /**
     * Resets the guessView and prompts the computer to speak the next remark prompting the user to make another guess
     */
    private void setUpNextGuess(){
        guessView.setText("");
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_UNTIL_CHECKIN - numGuesses));
        computerRemarkView.setText(COMPUTER_REMARKS[numGuesses]);
        voice.speak(COMPUTER_REMARKS[numGuesses], TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void playAgain(View view) {
        playAgain();
    }

    private void playAgain(){
        if (this.objectPool.size() != 0){
            voice.speak(PLAY_AGAIN_PROMPT_A, TextToSpeech.QUEUE_FLUSH, null, null);
            playAgainRequestInProgress = true;

        } else { //Go back to first screen and choose a new image
            voice.speak(PLAY_AGAIN_PROMPT_B, TextToSpeech.QUEUE_FLUSH, null, null);
            playAgainNewImage();
        }
    }

    private void playAgainSameImage(){
        setUpPlayForCurrentImage();
        String clue = getClue();
        voice.speak(COMPUTER_INIT + ISPY_PRELUDE + clue + COMPUTER_REMARKS[numGuesses], TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void playAgainNewImage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void giveClue(View view){
        giveClue();
    }

    private void giveClue(){
        String clue = getClue();
        this.numGuesses = 0;
        guessView.setText("");
        remainingGuessesView.setText("Number of Guesses remaining: " + (NUM_GUESSES_UNTIL_CHECKIN - numGuesses));
        voice.speak(ISPY_PRELUDE + clue + COMPUTER_REMARKS[numGuesses], TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * Randomly chooses a clueType from whatever clue types are available in the cluePool keys. Then randomly selects a clue from that
     * clueType to use for the current iSpyClue. Removes that clue from the cluePool so that there are no repeat clues
     */
    private String getClue(){
        TextView iSpyClueView = findViewById(R.id.iSpyClue);
        String iSpyClue = "";
        Random rand = new Random();
        ArrayList<String> clueTypes = new ArrayList<>();
        if(!cluePool.keySet().isEmpty()){
            clueTypes.addAll(cluePool.keySet());
            clueType = clueTypes.get(rand.nextInt(cluePool.keySet().size()));

            switch(clueType){
                case COLOR_CLUE:
                    iSpyClue = "is " + cluePool.get(COLOR_CLUE).get(0);
                    cluePool.remove(COLOR_CLUE);
                    break;
                case LOCATION_CLUE:
                    ArrayList<String> locations = cluePool.get(LOCATION_CLUE);
                    int i = rand.nextInt(locations.size());
                    iSpyClue = locations.get(i);
                    locations.remove(i);
                    if (locations.size() == 0) cluePool.remove(LOCATION_CLUE);
                    break;
                case WIKI_CLUE:
                    iSpyClue = cluePool.get(WIKI_CLUE).get(0);
                    cluePool.remove(WIKI_CLUE);
                    break;
                case CONCEPTNET_CLUE:
                    ArrayList<String> conceptNetClues = cluePool.get(CONCEPTNET_CLUE);
                    i = rand.nextInt(conceptNetClues.size());
                    iSpyClue = conceptNetClues.get(i);
                    conceptNetClues.remove(i);
                    if (conceptNetClues.size() == 0) cluePool.remove(CONCEPTNET_CLUE);
                    break;
            }
        } else {
            Log.i(TAG, "out of clues");
            iSpyClue = "out of clues";
        }

        iSpyClueView.setText(iSpyClue);
        numCluesGiven++;
        iSpyClue += ".";
        return iSpyClue;

//        voice.speak(ISPY_PRELUDE+ iSpyClue, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void getSpeechInput(View view){
        if (playAgainRequestInProgress){
            super.startSpeechRecognition(PLAY_AGAIN_REQUEST);
        } else if (checkinInProgress){
            super.startSpeechRecognition(CHECKIN_REQUEST);
        } else {
            super.startSpeechRecognition(GUESS_INPUT_REQUEST);
        }
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
            case CHECKIN_REQUEST:
                if (resultCode == RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String response = result.get(0);
                    if (response.contains("give up")){
                        resultView.setText(COMPUTER_WINS + chosenObject.getPrimaryLabel());
                        checkinInProgress = false;
                        voice.speak(COMPUTER_WINS + chosenObject.getPrimaryLabel(), TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (response.contains("another") || response.contains("clue") || response.contains("keep")){
                        checkinInProgress = false;
                        giveClue();
                    }
                }
        }
    }
}
