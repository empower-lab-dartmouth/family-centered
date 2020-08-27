package projects.android.aispy;

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
import java.util.List;
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
    private static HashSet<String> unhelpfulLabels;

    private final HashSet<String> COMMON_COLORS = new HashSet<>(Arrays.asList("white", "black", "grey", "red", "green", "blue", "yellow", "orange", "purple", "pink", "brown"));

    private static AISpyImage instance;

    public static AISpyImage getInstance(){
        return instance;
    }

    public static void setInstance(Context thisContext, File storageDir, String imagePath) throws IOException {
        instance = new AISpyImage(thisContext, storageDir, imagePath);
    }

    /**
     * Constructor to create an AISpyImage representation. This constructor is private so that AISpyImage is a "Singleton" (only one instantiation can exist at a time). This
     * allows the current instantiation of AISpyImage to be easily accessible from every class.
     * @param thisContext the Context that the picture was taken in
     * @param storageDir the File directory where the full AISpy image is stored
     * @param imagePath the String path where the full AISpy image is stored.
     * @throws IOException
     */
    private AISpyImage(Context thisContext, File storageDir, String imagePath) throws IOException {
        fillUnhelpfulLabels();

        this.fullImagePath = imagePath;

        /** 1) find all labels that can be detected for the full image **/
        List<FirebaseVisionImageLabel> labels = LabelDetectionAPI.getImageLabels(thisContext, imagePath, unhelpfulLabels);
        if (labels != null) {
            allLabels = new ArrayList<>(labels);
        } else {
            System.out.println("nothing detected");
            return;
        }


        //Create hash set to quickly check if a label is present or not
        HashSet<String> allLabelsSet = new HashSet<>();
        for (FirebaseVisionImageLabel label : allLabels){
            allLabelsSet.add(label.getText());
        }

        ArrayList<AISpyObject> aiSpyObjects = new ArrayList<>();


        /** 2) detect all detectable "objects" and get their boundary boxes **/
        ArrayList<DetectedObject> detectedObjects = (ArrayList<DetectedObject>) ObjectDetectionAPI.getObjectBoundaryBoxes(thisContext, imagePath);

        /** 3) loop through all detected objects and... **/
        for (DetectedObject detectedObject: detectedObjects){

            /** a) Crop out the corresponding bitmap for the detected object **/
            Bitmap croppedObject = BitmapAPI.getCroppedObject(detectedObject, imagePath);


            //Save the object image
            String newFilePath = createNewImageFile(storageDir);
            try {
                File file = new File(newFilePath);
                FileOutputStream fOut = new FileOutputStream(file);
                croppedObject.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
                fOut.flush();
                fOut.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            /** b) Find all labels in the cropped image of that detected object **/
            //Detect the labels for the object
            ArrayList<FirebaseVisionImageLabel> objectLabels;
            List<FirebaseVisionImageLabel> objectLabelsApiReturn = LabelDetectionAPI.getImageLabels(thisContext, newFilePath, unhelpfulLabels);
            if (objectLabelsApiReturn != null) {
                objectLabels = new ArrayList<>(LabelDetectionAPI.getImageLabels(thisContext, newFilePath, unhelpfulLabels));
            } else {
                System.out.println("nothing detected");
                return;
            }

            //Add to allLabels if not already there
            for (FirebaseVisionImageLabel label : objectLabels){
                if (!allLabelsSet.contains(label.getText())){
                    allLabelsSet.add(label.getText());
                    allLabels.add(label);
                }
            }

            /** c) Find the object's color **/
            //Find the dominant color in the object
            String color = (findColorInLabels(objectLabels));
            //create a new customized bitmap for color detection
            Bitmap croppedForColorObject = null;
            if(color == null){
                croppedForColorObject = BitmapAPI.getBitmapForCloud(detectedObject, imagePath);
            }

            /** d) Store all that data into an AISpyObject object**/
            if (objectLabels.size() > 0){
                AISpyObject aiSpyObject = new AISpyObject(croppedObject, croppedForColorObject, newFilePath, detectedObject.getBoundingBox(), objectLabels, color);
                aiSpyObjects.add(aiSpyObject);
            }
        }
        //Detect colors for those objects whose color == null
        ArrayList<AISpyObject> refineColorList = new ArrayList<>();

        //Add those objects without a color labels into a list
        for(AISpyObject object : aiSpyObjects) {
            if(object.getColor() == null){
                refineColorList.add(object);
            }
        }
        //remove these objects
        for(AISpyObject object : refineColorList) {
            aiSpyObjects.remove(object);
        }
        //add them back after naming their colors.
        aiSpyObjects.addAll(new ColorDetectorAPI(refineColorList, thisContext).getReturnList());
        System.out.println("Test time 2");

        allObjects = aiSpyObjects;

        /** 4) Create the ISpy Map to map objects to their corresponding features **/
        generateISpyMap(thisContext);
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
    private void generateISpyMap(Context thisContext){
        iSpyMap = new HashMap<>();


        for (AISpyObject object : allObjects){

            Features features = new Features();
            features.color = object.getColor();
            features.locations = generateLocationFeatures(object);
//            features.wiki = getWiki(object, thisContext);
            features.conceptNet = getConceptNet(object, thisContext);

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
     * getConceptNet calls ConceptNetAPI.getConceptNetMap and returns the first concept net map that is populated with meaningful content
     * Which ever label creates the most populated concept net is stored as the AISpyObject's "primary label"
     * @param obj
     * @param thisContext
     * @return
     */
    private HashMap<String, ArrayList<String>> getConceptNet(AISpyObject obj, Context thisContext){
        HashMap<String, ArrayList<String>> conceptNetMap = null;
        for (String label : obj.getPossibleLabels()){
            HashMap<String, ArrayList<String>> possibleConceptNetMap = ConceptNetAPI.getConceptNetMap(label, thisContext);
            if (ConceptNetAPI.getConceptNetMapScore(possibleConceptNetMap) != 0){
                conceptNetMap = possibleConceptNetMap;
                obj.setPrimaryLabel(label);
                return conceptNetMap;
            }
        }

        return conceptNetMap;
    }

    /**
     * Creates a hash set with labels commonly returned by Google Firebase Label Detector API that are unhelpful in a game of AISpy
     */
    private static void fillUnhelpfulLabels(){
        unhelpfulLabels = new HashSet<>();
        unhelpfulLabels.add("furniture");
        unhelpfulLabels.add("property");
        unhelpfulLabels.add("wood");
        unhelpfulLabels.add("mammal");
        unhelpfulLabels.add("canidae");
        unhelpfulLabels.add("electronics");
        unhelpfulLabels.add("interior design");
        unhelpfulLabels.add("darkness");
        unhelpfulLabels.add("light");
        unhelpfulLabels.add("automotive exterior");
        unhelpfulLabels.add("technology");
        unhelpfulLabels.add("tan");
        unhelpfulLabels.add("pattern");
        unhelpfulLabels.add("sky");
        unhelpfulLabels.add("land vehicle");
        unhelpfulLabels.add("cobalt blue");
        unhelpfulLabels.add("vertebrae");
    }
}


