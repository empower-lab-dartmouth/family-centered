/*
This project was extracted from the Android App in the https://github.com/StanfordHCI/family-centered repo. I extracted only the code relevant to image processing for
AI Spy (and perhaps Explore Mode). The intention is to have a bare bones project space where we can focus on the more technical challenge of developing an excellent
back-end for AI Spy image processing which we will then build the rest of AI Spy around.

One strange bug exists in both this project and the family-centered Android App project where occasionally the project won't compile due to a build error. All you need to do is click
Build -> Clean Project and then try running again and it will compile.
 */

package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.v4.content.FileProvider;
import android.view.View;

import androidx.core.content.FileProvider; //added

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;


public class MainActivity extends BasicFunctionality{

    String currentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void takePicture(View view) {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "edu.stanford.curis.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                System.out.println("******************" + takePictureIntent);
                System.out.println("******************" + photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

//    private String createNewImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        String newImagePath = image.getAbsolutePath();
//
//        return newImagePath;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Context thisContent = this.getApplicationContext();

//            //Asynchronously run APIs to collect data about the image TODO: Most likely we will move this code to whatever activity follows taking the picture (an updated DisplayImageActivity)
//            new AsyncTask<Object, Void, ArrayList<Rect>>() {
//                @Override
//                protected ArrayList<Rect> doInBackground(Object... params) { //TODO: Once all apis are implemented, this should return the full image data structure that we want to build (A map of colors to objects)
//                    ArrayList<Rect> objectBoundaryBoxes = new ArrayList<Rect>();
//                    try {
//
//                        //This api uses Firebase ML Kit to locate objects in the image and returns their boundary boxes
//                        objectBoundaryBoxes = ObjectDetectionAPI.getObjectBoundaryBoxes(thisContent, currentPhotoPath);
//
//                        Bitmap sample = ObjectCropperAPI.getCroppedObjects(objectBoundaryBoxes, currentPhotoPath);
//
//                        String newFilePath = createNewImageFile();
//                        try {
//                            File file = new File(newFilePath);
//                            FileOutputStream fOut = new FileOutputStream(file);
//                            sample.compress(Bitmap.CompressFormat.PNG, 85, fOut);
//                            fOut.flush();
//                            fOut.close();
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                    }catch (Exception e) {
//
//                    }
//
//                    return objectBoundaryBoxes;
//                }
//
//                protected void onPostExecute(ArrayList<Rect> objectBoundaryBoxes) {
//                    for(Rect boundaryBox : objectBoundaryBoxes){
//                        System.out.println("*********************" + boundaryBox);
//                    }
//
//
//                }
//            }.execute();

            //This calls the next activity (Display Image Activity) which uses old code based on the Cloud Vision api to collect info about the image
            Intent intent = new Intent(this, DisplayImageActivity.class);
            intent.putExtra("image_path", currentPhotoPath);
            startActivity(intent);
        }
    }

    public void gallerySelect(View view) {

    }
}