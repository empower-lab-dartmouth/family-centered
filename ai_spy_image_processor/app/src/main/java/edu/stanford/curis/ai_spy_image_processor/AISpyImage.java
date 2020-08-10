package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

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

/**
 * AISpyImage is a representation of an image containing all information necessary for a computer to play "I Spy"
 * Initialization of an AISpyImage object requires several network calls to Google Vision APIs so it must be run in a sub thread
 */
public class
AISpyImage implements Serializable {
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

    /**
     * Constructor to create an AISpyImage representation. This constructor is private so that AISpyImage is a "Singleton" (only one instantiation can exist at a time). This
     * allows the current instantiation of AISpyImage to be easily accessible from every class.
     * @param thisContent the Context that the picture was taken in
     * @param storageDir the File directory where the full AISpy image is stored
     * @param imagePath the String path where the full AISpy image is stored.
     * @throws IOException
     */
    private AISpyImage(Context thisContent, File storageDir, String imagePath) throws IOException {

        this.fullImagePath = imagePath;

        allLabels = new ArrayList<>(LabelDetectionAPI.getImageLabels(thisContent, imagePath));

        //Create hash set to quickly check if a label is present or not
        HashSet<String> allLabelsSet = new HashSet<>();
        for (FirebaseVisionImageLabel label : allLabels){
            allLabelsSet.add(label.getText());
        }

        ArrayList<AISpyObject> aiSpyObjects = new ArrayList<>();

        //Locate objects in the image and get their boundary boxes
        ArrayList<DetectedObject> detectedObjects = (ArrayList<DetectedObject>) ObjectDetectionAPI.getObjectBoundaryBoxes(thisContent, imagePath);

        //Get each object's dominant color and store cropped bitmaps in files
        for (DetectedObject detectedObject: detectedObjects){

            Bitmap croppedObject = BitmapAPI.getCroppedObject(detectedObject, imagePath);

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

            //Add to allLabels if not already there
            for (FirebaseVisionImageLabel label : objectLabels){
                if (!allLabelsSet.contains(label.getText())){
                    allLabelsSet.add(label.getText());
                    allLabels.add(label);
                }
            }

            //Find the dominant color in the object (only send to colorDetector if there isn't already a label with a color)
            String color = (findColorInLabels(objectLabels));
            if(color == null){ color = new ColorDetectorAPI(croppedObject, thisContent).getColor(); }

            //Store object info in AISpyObject
            if (objectLabels.size() > 0){
                AISpyObject aiSpyObject = new AISpyObject(croppedObject, newFilePath, detectedObject.getBoundingBox(), objectLabels, color);
                aiSpyObjects.add(aiSpyObject);
            }


        }
        allObjects = aiSpyObjects;

        generateISpyMap(thisContent);
    }

    /****** Public Methods *******/

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

    /**
     * Loops through the labels of a detected object to see if a color was detected
     * @param labels: a list of FirebaseVisionImageLabel detected for an object
     * @return null if no color found; the String color name if a color is found
     */
    private String findColorInLabels(ArrayList<FirebaseVisionImageLabel> labels){
        String trueColor = null;
        Boolean foundTrueColor = false;
        ArrayList<FirebaseVisionImageLabel> toRemove = new ArrayList<>();

        for (FirebaseVisionImageLabel label : labels){
            String text = label.getText().toLowerCase();
            if (COMMON_COLORS.contains(text)){
                if (!foundTrueColor){
                    trueColor = text;
                    foundTrueColor = true;
                }
                toRemove.add(label);
            }
        }

        for(FirebaseVisionImageLabel label : toRemove){
            labels.remove(label);
        }
        return trueColor;
    }


    /**
     * Creates and returns an image file name
     * @throws IOException
     */
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

    /**
     * Creates a hashmap mapping each detected object to the objects Features
     */
    private void generateISpyMap(Context thisContent){
        iSpyMap = new HashMap<>();


        for (AISpyObject object : allObjects){

            Features features = new Features();
            features.color = object.getColor();
            features.locations = generateLocationFeatures(object);
            features.wiki = getWiki(object, thisContent);
            features.conceptNet = getConceptNet(object, thisContent);

            iSpyMap.put(object, features);
        }
    }

    /**
     * In relation to one detected object, finds all other objects that share a relative location to it \. Stores data as
     * a hashmap mapping the type of relative location ("right of", "left of", "above", "below") to the AISpyObject which is the
     * base of that relative location. For example, the location features of one object might include:
     * String("right of") -> HashSet({'stop sign', 'car', 'sidewalk'})
     */
    private HashMap<String,HashSet<AISpyObject>> generateLocationFeatures(AISpyObject obj){
        HashSet<AISpyObject> above = new HashSet<>();
        HashSet<AISpyObject> below = new HashSet<>();
        HashSet<AISpyObject> rightOf = new HashSet<>();
        HashSet<AISpyObject>leftOf = new HashSet<>();

        Rect objLocation = obj.getLocation();

        //Find which objects are below/above, to the right/left of other objects
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

        HashMap<String,HashSet<AISpyObject>> locationMap = new HashMap<>();
        if (above.size() > 0) locationMap.put("above", above);
        if (below.size() > 0) locationMap.put("below", below);
        if (rightOf.size() > 0) locationMap.put("right", rightOf);
        if (leftOf.size() > 0) locationMap.put("left", leftOf);

        return locationMap;
    }

    /**
     * Loops through the labels of an object and for whichever one first successfully queries wiki, returns that result
     */
    private String getWiki(AISpyObject obj, Context thisContent){
        for (String label : obj.getPossibleLabels()){
            String wiki = WikiClueAPI.getWikiClue(label, thisContent);
            if (wiki != null){
                return wiki;
            }
        }

        return null;
    }

    private HashMap<String, ArrayList<String>> getConceptNet(AISpyObject obj, Context thisContent){
        HashMap<String, ArrayList<String>> conceptNetMap = null;
        int bestScore = 0;
        for (String label : obj.getPossibleLabels()){
            HashMap<String, ArrayList<String>> possibleConceptNetMap = ConceptNetAPI.getConceptNetMap(label, thisContent);
            if (ConceptNetAPI.getConceptNetMapScore(possibleConceptNetMap) != 0){
                conceptNetMap = possibleConceptNetMap;
                return conceptNetMap;
            }
//            int curScore = ConceptNetAPI.getConceptNetMapScore(possibleConceptNetMap);
//            if (curScore > bestScore){
//                bestScore = curScore;
//                conceptNetMap = possibleConceptNetMap;
//            }
        }

        return conceptNetMap;
    }
}


