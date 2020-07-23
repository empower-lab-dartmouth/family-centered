package edu.stanford.curis.ai_spy_image_processor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class PlayWithComputerSpyActivity extends BasicFunctionality {

    private AISpyImage aiSpyImage;
    private String iSpyClue;
    private AISpyObject chosenObject;
    private TextToSpeech voice;

    //String constants
    private final String[] CHILD_INCORRECT_GUESS = new String[]{"Sorry, try again", "That's still not right, sorry. Try again!", "I'm thinking of something else, try again!", "Wanna give up?"};
    private final String MOTIVATION = "You can do it!";
    private final String COMPUTER_WINS = "Gotcha! One point for me. It's the ";
    private final String CHILD_CORRECT_FIRST_TRY = "Wow, you're right on the first try! One point for you";



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

        setUpAIVoice();
        setUpGame();

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
                }
            }
        });

        voice.setPitch(0.9f);
        voice.setSpeechRate(0.7f);
    }

    private void setUpGame(){
        this.aiSpyImage = AISpyImage.getInstance();

        //Clear old views
        TextView resultView = findViewById(R.id.result);
        resultView.setText("");
        EditText guessView = findViewById(R.id.guess);
        guessView.setText("");
        TextView iSpyClueView = findViewById(R.id.iSpyClue);
        iSpyClueView.setText("");

        setISpyImage();
        chosenObject = aiSpyImage.chooseRandomObject();

    }

    private void setISpyImage(){
        ImageView fullImage = findViewById(R.id.fullImage);
        Bitmap fullImageBitmap = BitmapFactory.decodeFile(aiSpyImage.getFullImagePath());
        fullImage.setImageBitmap(fullImageBitmap);
    }


    public void checkGuess(View view) {
        EditText guessView = findViewById(R.id.guess);

        String guess = guessView.getText().toString();

        TextView resultView = findViewById(R.id.result);

        ArrayList<String> possibleAnswers = chosenObject.getPossibleLabels();

        for (String possibleAnswer: possibleAnswers){
            if (guess.toUpperCase().contains(possibleAnswer)){
                resultView.setText("Correct!");
                return;
            }
        }

        //If possible answer can not be found in guess
        resultView.setText("Wrong :( The correct answer is " + chosenObject.getPossibleLabels().get(0));
    }

    public void playAgain(View view) {
        setUpGame();
    }

    public void giveColorClue(View view) {
        TextView iSpyClueView = findViewById(R.id.iSpyClue);
        Features features = aiSpyImage.getiSpyMap().get(chosenObject);
        iSpyClue = features.color;
        iSpyClueView.setText(iSpyClue);
        voice.speak("I spy something " + iSpyClue, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void giveLocationClue(View view) {
        Random rand = new Random();
        TextView iSpyClueView = findViewById(R.id.iSpyClue);
        Features features = aiSpyImage.getiSpyMap().get(chosenObject);

//        iSpyClue = features.locations.toArray(new String[features.locations.size()])[rand.nextInt(features.locations.size())];

        int numDirections = features.locations.keySet().size();
        String direction = features.locations.keySet().toArray(new String[numDirections])[rand.nextInt(numDirections)]; //Get random direction from location features
        int numObjectsForDirection = features.locations.get(direction).size();
        AISpyObject object = features.locations.get(direction).toArray(new AISpyObject[numObjectsForDirection])[rand.nextInt(numObjectsForDirection)]; //Get random object from chosen direction
        int numLabelsForObject = object.getPossibleLabels().size(); //Get random label from chosen object
        String label = object.getPossibleLabels().get(rand.nextInt(numLabelsForObject));

        if (direction == "above" || direction == "below"){
            iSpyClue = direction + " the " + label.toLowerCase();
        } else if (direction == "right" || direction == "left"){
            iSpyClue = "to the " + direction + " of the " + label.toLowerCase();
        }

        iSpyClueView.setText(iSpyClue);
        voice.speak("I spy something " + iSpyClue, TextToSpeech.QUEUE_FLUSH, null);
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
                    System.out.println("*********************************" + result.get(0));
                    EditText guessView = findViewById(R.id.guess);
                    guessView.setText(result.get(0));
                }
        }
    }
}
