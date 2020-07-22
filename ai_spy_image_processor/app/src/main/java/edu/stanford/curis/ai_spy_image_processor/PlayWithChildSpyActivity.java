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

    private AISpyImage aiSpyImage;
    private HashMap<AISpyObject, Features> iSpyMap;
    private String iSpyClue;
    private AISpyObject computerGuess;
    private TextToSpeech voice;
    private int numRemainingGuesses;

    //Views
    TextView guessView;
    EditText iSpyClueView;
    TextView remainingGuessesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_spy);

        this.aiSpyImage = AISpyImage.getInstance();
        this.iSpyMap = aiSpyImage.getiSpyMap();
        this.numRemainingGuesses = 5;

        //Set views
        guessView = findViewById(R.id.computerGuess);
        guessView.setText("");
        iSpyClueView = findViewById(R.id.iSpyClue);
        iSpyClueView.setText("");
        remainingGuessesView = findViewById(R.id.remainingGuesses);
        remainingGuessesView.setText("Number of Guesses remaining: " + numRemainingGuesses);

        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapFactory.decodeFile(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
    }

    public void startComputerGuessing(View view) {
        findPotentialAnswer();
        makeGuess();
    }

    private void findPotentialAnswer(){
        iSpyClue = iSpyClueView.getText().toString();

        for (String commonColor : commonColors){
            if (iSpyClue.contains(commonColor)){
                //Check objects with that color
                computerGuess = checkColors();
                return;
            }
        }

        for (String commonRelativeLocation: commonRelativeLocations){
            if (iSpyClue.contains(commonRelativeLocation)){
                //check objects with that relative location
                computerGuess = checkLocations();
                return;
            }
        }
    }

    private void makeGuess(){
        if (computerGuess != null){
            guessView.setText(computerGuess.getPossibleLabels().get(0));
        }
        else{
            guessView.setText(aiSpyImage.getAllLabels().get(0).getText());
        }
    }

    private AISpyObject checkColors(){
        for (AISpyObject object: iSpyMap.keySet()){
            if (iSpyClue.contains(iSpyMap.get(object).color)){
                return object;
            }
        }

        return null;
    }

    private AISpyObject checkLocations(){ //TODO: Change location feature structure to be hash map of "right, left, above, below (etc)" mapping to objects they are relative to
        for (AISpyObject object: iSpyMap.keySet()){
            for (String relativeLocation : iSpyMap.get(object).locations){
                if (iSpyClue.contains(relativeLocation)){
                    return object;
                }
            }
        }

        return null;
    }


    public void getSpeechInput(View view) {
    }

    public void checkGuess(View view) {
    }

    public void playAgain(View view) {
    }
}
