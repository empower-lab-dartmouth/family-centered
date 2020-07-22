package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.mlkit.vision.objects.DetectedObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/*
AISpyImage is a representation of an image containing all information necessary for a computer to play "I Spy"
Initialization of an AISpyImage object requires several network calls to Google Vision APIs so it must be run in a sub thread
 */
public class AISpyImage implements Serializable {
    private String fullImagePath;
    private HashMap<AISpyObject, Features> iSpyMap;
    private ArrayList<AISpyObject> allObjects;
    private ArrayList<FirebaseVisionImageLabel> allLabels;
    private AISpyObject correct;

    private final HashSet<String> COMMON_COLORS = new HashSet<>(Arrays.asList("white", "black", "grey", "red", "green", "blue", "yellow", "orange", "purple", "pink", "brown"));

    private static AISpyImage instance;

    public static AISpyImage getInstance(){
        return instance;
    }

    public static void setInstance(Context thisContent, File storageDir, String imagePath) throws IOException {
        instance = new AISpyImage(thisContent, storageDir, imagePath);
    }

    private AISpyImage(Context thisContent, File storageDir, String imagePath) throws IOException {

        this.fullImagePath = imagePath;

        allLabels = new ArrayList<>(LabelDetectionAPI.getImageLabels(thisContent, imagePath));

        ArrayList<Bitmap> croppedObjects;
        ArrayList<DetectedObject> detectedObjects;
        ArrayList<AISpyObject> aiSpyObjects = new ArrayList<>();

        //Locate objects in the image and get their boundary boxes
        detectedObjects = (ArrayList<DetectedObject>) ObjectDetectionAPI.getObjectBoundaryBoxes(thisContent, imagePath);

        //Get each object's dominant color and store cropped bitmaps in files
        for (DetectedObject detectedObject: detectedObjects){

            Bitmap croppedObject = ObjectCropperAPI.getCroppedObject(detectedObject, imagePath);

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
            ArrayList<FirebaseVisionImageLabel> objectLabels = new ArrayList<>(LabelDetectionAPI.getImageLabels(thisContent, newFilePath));

            //Find the dominant color in the object //TODO: Only send to colorDetector if there isn't already a label with a color
            String color = (findColorInLabels(objectLabels));
            if(color == null){ color = new ColorDetectorAPI(croppedObject).getColor(); }

            //Store object info in AISpyObject
            if (objectLabels.size() > 0){
                AISpyObject aiSpyObject = new AISpyObject(croppedObject, newFilePath, detectedObject.getBoundingBox(), objectLabels, color);
                aiSpyObjects.add(aiSpyObject);
            }


        }
        allObjects = aiSpyObjects;

        generateISpyMap();
    }

    public AISpyObject chooseRandomObject(){
        Random rand = new Random();
        return allObjects.get(rand.nextInt(allObjects.size()));
    }

    public ArrayList<AISpyObject> getAllObjects() {
        return allObjects;
    }

    public String getFullImagePath() {
        return fullImagePath;
    }

    public ArrayList<FirebaseVisionImageLabel> getAllLabels() {
        return allLabels;
    }

    public String getAllLabelsText(){
        String allLabelsText = "";

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        for (FirebaseVisionImageLabel label : this.allLabels){
            allLabelsText += (label.getText() + " " + decimalFormat.format(label.getConfidence()) + "\n");
        }

        return allLabelsText;
    }

    public HashMap<AISpyObject, Features> getiSpyMap() {
        return iSpyMap;
    }

    private String findColorInLabels(ArrayList<FirebaseVisionImageLabel> labels){
        for (FirebaseVisionImageLabel label : labels){
            if (COMMON_COLORS.contains(label.getText().toLowerCase())){
                labels.remove(label);
                return label.getText();
            }
        }
        return null;
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

    private void generateISpyMap(){
        iSpyMap = new HashMap<>();


        for (AISpyObject object : allObjects){

            Features features = new Features();
            features.color = object.getColor();
            features.locations = generateLocationFeatures(object);

            iSpyMap.put(object, features);
        }
    }

    private HashSet<String> generateLocationFeatures(AISpyObject obj){
        HashSet<AISpyObject> above = new HashSet<>();
        HashSet<AISpyObject> below = new HashSet<>();
        HashSet<AISpyObject> rightOf = new HashSet<>();
        HashSet<AISpyObject>leftOf = new HashSet<>();

        Rect objLocation = obj.getLocation();

        for (AISpyObject otherObj : allObjects){
            Rect otherObjLocation = otherObj.getLocation();
            if (objLocation.top > otherObjLocation.bottom){
                below.add(otherObj);
            } else if (objLocation.bottom < otherObjLocation.top){
                above.add(otherObj);
            }

            if (objLocation.right < otherObjLocation.left){
                leftOf.add(otherObj);
            } else if (objLocation.left > otherObjLocation.right){
                rightOf.add(otherObj);
            }
        }

        HashSet<String> locationFeatures = new HashSet<>();

        for (AISpyObject aboveObj : above){
            for (String label : aboveObj.getPossibleLabels()){
                locationFeatures.add("above the " + label);
            }
        }
        for (AISpyObject belowObj : below){
            for (String label : belowObj.getPossibleLabels()){
                locationFeatures.add("below the " + label);
            }
        }
        for (AISpyObject rightObj : rightOf){
            for (String label : rightObj.getPossibleLabels()){
                locationFeatures.add("to the right of the " + label);
            }
        }
        for (AISpyObject leftObj : leftOf){
            for (String label : leftObj.getPossibleLabels()){
                locationFeatures.add("to the left of the " + label);
            }
        }

        return locationFeatures;
    }



}


