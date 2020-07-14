package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.mlkit.vision.objects.DetectedObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/*
AISpyImage is a representation of an image containing all information necessary for a computer to play "I Spy"
Initialization of an AISpyImage object requires several network calls to Google Vision APIs so it must be run in a sub thread
 */
public class AISpyImage {
    private HashMap<String, AISpyObject> colorToObjectMap;
    private ArrayList<AISpyObject> allObjects;

    public AISpyImage(Context thisContent, File storageDir, String imagePath) throws IOException {

        ArrayList<Bitmap> croppedObjects;
        ArrayList<DetectedObject> detectedObjects;
        ArrayList<AISpyObject> aiSpyObjects = new ArrayList<>();

        //Locate objects in the image and get their boundary boxes
        detectedObjects = (ArrayList<DetectedObject>) ObjectDetectionAPI.getObjectBoundaryBoxes(thisContent, imagePath);

        //Crop out each object by getting the a cropped bitmap for each boundary box
        croppedObjects = ObjectCropperAPI.getCroppedObjects(detectedObjects, imagePath);

        //Get each object's dominant color and store cropped bitmaps in files
        for (Bitmap croppedObject: croppedObjects){

            //Find the dominant color in the object
            String color = new ColorDetectorAPI(croppedObject).getColor();

            //Save the object image
            String newFilePath = createNewImageFile(storageDir);
            try {
                File file = new File(newFilePath);
                FileOutputStream fOut = new FileOutputStream(file);
                croppedObject.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //Detect the labels for the object
            ArrayList<FirebaseVisionImageLabel> labels = new ArrayList<>(LabelDetectionAPI.getImageLabels(thisContent, newFilePath));

            //Store object info in AISpyObject
            AISpyObject aiSpyObject = new AISpyObject(croppedObject, newFilePath, labels, color);
            aiSpyObjects.add(aiSpyObject);


        }
        allObjects = aiSpyObjects;

        colorToObjectMap = new HashMap<>();

        for (AISpyObject object : aiSpyObjects){
            colorToObjectMap.put(object.getColor(), object);
        }
    }

    // Creates an image file name
    private String createNewImageFile(File storageDir) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String newImagePath = image.getAbsolutePath();

        return newImagePath;
    }

    public ArrayList<AISpyObject> getAllObjects() {
        return allObjects;
    }
}
