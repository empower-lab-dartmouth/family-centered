package projects.android.aispy;

import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.appcompat.app.AppCompatActivity;

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
public class PlayWithComputerSpyActivity extends AppCompatActivity {

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


    private final String COLOR_CLUE = "COLOR";
    private final String LOCATION_CLUE = "LOCATION";
    private final String WIKI_CLUE = "WIKI";
    private final String CONCEPTNET_CLUE= "CONCEPTNET";
    private final int GUESS_INPUT_REQUEST = 10;
    public final String TAG = "COMPUTER_SPY";

    private int numCluesGiven;
    private String clueType;
    HashMap<String, ArrayList<String>> cluePool;


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
        Features features = aiSpyImage.getiSpyMap().get(chosenObject);
        cluePool = makeCluePool(features);
        numCluesGiven = 0;
    }

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

    private void setISpyImage(){
        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapAPI.getCorrectOrientation(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
    }


    public void checkGuess(View view){
        String guess = guessView.getText().toString();
        checkGuess(guess);
    }


    private void checkGuess(String guess) {
//        String guess = guessView.getText().toString();
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
            voice.speak(CHILD_CORRECT_FIRST_TRY, TextToSpeech.QUEUE_FLUSH, null, null);
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

    /**
     * Randomly chooses a clueType from whatever clue types are available in the cluePool keys. Then randomly selects a clue from that
     * clueType to use for the current iSpyClue. Removes that clue from the cluePool so that there are no repeat clues
     * @param view
     */
    public void giveClue(View view){
        TextView iSpyClueView = findViewById(R.id.iSpyClue);
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

        voice.speak(ISPY_PRELUDE+ iSpyClue, TextToSpeech.QUEUE_FLUSH, null, null);

    }

    //https://www.youtube.com/watch?v=0bLwXw5aFOs
    public void getSpeechInput(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, GUESS_INPUT_REQUEST);
        } else {
            Toast.makeText(this, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
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
        }
    }


}
