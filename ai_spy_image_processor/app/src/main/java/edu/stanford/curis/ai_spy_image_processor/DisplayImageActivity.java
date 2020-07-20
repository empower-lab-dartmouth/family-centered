package edu.stanford.curis.ai_spy_image_processor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Base64;
import android.widget.ImageView;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.ColorInfo;
import com.google.api.services.vision.v1.model.DominantColorsAnnotation;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageProperties;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.mlkit.vision.objects.DetectedObject;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;




public class DisplayImageActivity extends BasicFunctionality {
    private ImageView imageView;
    private Bitmap bitmap;
    private Feature feature;
    private Spinner spinnerVisionAPI;
    private TextView visionAPIData;


    private String[] visionAPI = new String[]{"LABEL_DETECTION","LANDMARK_DETECTION", "LOGO_DETECTION", "SAFE_SEARCH_DETECTION", "IMAGE_PROPERTIES"};
    private String api = visionAPI[0];

    private static final String CLOUD_VISION_API_KEY = "AIzaSyBrjvW-v6XkEC6XxO_GarOZbxfalDwfzvc";

    private static final String TAG = "API Activity";

    public static final String SERVER_PICTURE_POST = "http://10.0.2.2:3000/label_picture";

    private String imagePath;
    private AISpyImage aiSpyImage;




    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image_layout);
        imageView = findViewById(R.id.imageView);
        Bitmap picture = BitmapFactory.decodeFile(getIntent().getStringExtra("image_path"));
        imageView.setImageBitmap(picture);

        imagePath = getIntent().getStringExtra("image_path");

        visionAPIData = findViewById(R.id.visionApiText);
        visionAPIData.setText("Processing...");

        Context thisContent = this.getApplicationContext();


        //Asynchronously run APIs to collect data about the image
//        new AsyncTask<Object, Void, ArrayList<AISpyObject>>() {
        new AsyncTask<Object, Void, AISpyImage>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected AISpyImage doInBackground(Object... params) { //TODO: Once all apis are implemented, this should return the full image data structure that we want to build (A map of colors to objects)

                try {
                    AISpyImage.setInstance(thisContent, getExternalFilesDir(Environment.DIRECTORY_PICTURES), imagePath);
                    AISpyImage aiSpyImage = AISpyImage.getInstance();
                    return aiSpyImage;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(AISpyImage generatedAiSpyImage) {

                TextView allLabelsView = findViewById(R.id.visionApiText);
                String allLabelsText = generatedAiSpyImage.getAllLabelsText();
                visionAPIData.setText(allLabelsText);

                ImageView[] objectImages = {findViewById(R.id.objectView1), findViewById(R.id.objectView2), findViewById(R.id.objectView3), findViewById(R.id.objectView4), findViewById(R.id.objectView5), findViewById(R.id.objectView6)};
                TextView[] objectText = {findViewById(R.id.objectText1), findViewById(R.id.objectText2), findViewById(R.id.objectText3), findViewById(R.id.objectText4), findViewById(R.id.objectText5), findViewById(R.id.objectText6)};

                for (int i = 0; i < generatedAiSpyImage.getAllObjects().size() && i < objectImages.length; i++){
                    objectImages[i].setImageBitmap(generatedAiSpyImage.getAllObjects().get(i).getImage());
                    objectText[i].setText(generatedAiSpyImage.getAllObjects().get(i).getColor() + "\n\n" + generatedAiSpyImage.getAllObjects().get(i).getLabelsText());
                }

                System.out.println(generatedAiSpyImage);

                aiSpyImage = generatedAiSpyImage;
            }
        }.execute();

    }

    public void playAISpy(View view) {


        if (aiSpyImage != null) {
            Intent intent = new Intent(this, PlayAISpyActivity.class);
            startActivity(intent);
        } else {
            Toast toast=Toast.makeText(getApplicationContext(),"AI Spy is not ready", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
