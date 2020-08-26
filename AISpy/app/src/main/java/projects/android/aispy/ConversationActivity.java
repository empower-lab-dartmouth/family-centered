package projects.android.aispy;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Extended in all activities that require both TextToSpeech and speech recognition
 */
public class ConversationActivity extends AppCompatActivity {
    protected TextToSpeech voice;
    private boolean aiIsSpeaking;

    /**
     * Destroys the TextToSpeech voice when the activity is ended
     */
    @Override
    protected void onDestroy() {
        if (voice != null){
            voice.stop();
            voice.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Initializes the TextToSpeech voice
     * //TODO: change the voice to be less annoying
     */
    protected void setUpAIVoice(String initMessage){
        aiIsSpeaking = false;
        voice = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = voice.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported");
                    } else {

                    }

                    voice.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            // Log.d("MainActivity", "TTS finished");
                            aiIsSpeaking = false;

                            //TODO: add UI code to light up the user box

                        }

                        @Override
                        public void onError(String utteranceId) {
                        }

                        @Override
                        public void onStart(String utteranceId) {
                            aiIsSpeaking = true;

                            //TODO: add UI code to light up the ai box
                        }
                    });

                    voice.speak(initMessage, TextToSpeech.QUEUE_FLUSH, null, initMessage);
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

    /**
     * Calls android's built in Intent for speech recognition
     * @param request is an integer that is returned in onActivityResult so that the developer can align responses to the specific speech recognition request given in code
     */
    //https://www.youtube.com/watch?v=0bLwXw5aFOs
    protected void startSpeechRecognition(int request){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, request);
        } else {
            Toast.makeText(this, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @return true if ai is currently speaking, false if otherwise
     */
    protected boolean isAISpeaking(){
        return aiIsSpeaking;
    }
}
