package edu.stanford.curis.ai_spy_image_processor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.api.services.vision.v1.model.Feature;


import java.io.IOException;
import java.util.ArrayList;

/**
 * DisplayImageActivity is called as an Intent immediately after a user successfully takes a picture. In this activity,
 * the an AISpyImage singleton object is created as a representation of the image including all relevant meta-data. After
 * the AISpyImage is created, the user can navigate to the WelcomeActivity Intent and play a game of AISpy
 */
public class DisplayImageActivity extends BasicFunctionality {
    private ImageView imageView;
    private Bitmap bitmap;
    private Feature feature;
    private Spinner spinnerVisionAPI;
    private TextView visionAPIData;


    private String[] visionAPI = new String[]{"LABEL_DETECTION","LANDMARK_DETECTION", "LOGO_DETECTION", "SAFE_SEARCH_DETECTION", "IMAGE_PROPERTIES"};
    private String api = visionAPI[0];

    private String imagePath;
    private AISpyImage aiSpyImage;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image_layout);
        imageView = findViewById(R.id.imageView);
        imagePath = getIntent().getStringExtra("image_path");
//        Bitmap picture = BitmapFactory.decodeFile(imagePath);
//
//        //correct Orientation
//        try {
//            ExifInterface exif = new ExifInterface(imagePath);
//            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            int rotationInDegrees = exifToDegrees(rotation);
//            Matrix matrix = new Matrix();
//            if (rotation != 0) {matrix.preRotate(rotationInDegrees);}
//            Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, picture.getWidth(), picture.getHeight(), true);
//            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
//            imageView.setImageBitmap(rotatedBitmap);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Bitmap picture = BitmapAPI.getCorrectOrientation(imagePath);
        imageView.setImageBitmap(picture);
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90);
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, picture.getWidth(), picture.getHeight(), true);
//        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
//        imageView.setImageBitmap(rotatedBitmap);





        visionAPIData = findViewById(R.id.visionApiText);
        visionAPIData.setText("Processing...");

        Context thisContent = this.getApplicationContext();


        //Asynchronously creates the AISpyImage representation
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

                ArrayList<AISpyObject> allObjects = generatedAiSpyImage.getAllObjects();
                for (int i = 0; i < allObjects.size() && i < objectImages.length; i++){
                    AISpyObject object = allObjects.get(i);
                    objectImages[i].setImageBitmap(allObjects.get(i).getImage());
                    String wiki = generatedAiSpyImage.getiSpyMap().get(object).wiki;
                    if ( wiki == null) wiki = "";
                    objectText[i].setText(object.getColor() + "\n\n" + wiki+ "\n\n" + object.getLabelsText());
                }

                System.out.println(generatedAiSpyImage);

                aiSpyImage = generatedAiSpyImage;
            }
        }.execute();

    }

    /**
     * Starts the WelcomeActivity Intent
     * @param view
     */
    public void playAISpy(View view) {
        if (aiSpyImage != null) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        } else {
            Toast toast=Toast.makeText(getApplicationContext(),"AI Spy is not ready", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
