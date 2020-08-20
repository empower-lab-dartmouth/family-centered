package projects.android.aispy;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * An AISpyObject contains the image wherein that object is located, the primary color of that object, and an ArrayList of the labels firebase generated to describe that object
 */
public class AISpyObject implements Serializable {
    private String imageFileName;
    private Bitmap image;
    private Bitmap imageForColorDetection;
    private String primaryLabel;
    private ArrayList<FirebaseVisionImageLabel> labels;
    private String color;
    private Rect location;

    public AISpyObject(Bitmap image, Bitmap imageForColorDetection, String imageFileName, Rect location, ArrayList<FirebaseVisionImageLabel> labels, String color){
        this.image = image;
        this.imageForColorDetection = imageForColorDetection;
        this.imageFileName = imageFileName;
        this.location = location;
        this.labels = labels;
        this.color = color;
    }

    public void setPrimaryLabel(String primaryLabel) {
        this.primaryLabel = primaryLabel;
    }

    public String getPrimaryLabel(){
        if (this.primaryLabel == null){
            return labels.get(0).getText().toLowerCase();
        }
        return this.primaryLabel;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public Bitmap getImage(){
        return this.image;
    }

    public Bitmap getImageForColorDetection() { return this.imageForColorDetection; }

    /**
     * @return labelsText is a string representation of all the detected labels and their confidence levels
     */
    public String getLabelsText(){
        String labelsText = "";

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        for (int i = 0; i < 6 && i < labels.size(); i++){ //Stopping at a max of 6 labels just because I don't want the UI to get filled with too many labels
            labelsText += (labels.get(i).getText() + " " + decimalFormat.format((labels.get(i).getConfidence())) + "\n");
        }

        return labelsText;
    }

    /**
     * @return possibleLabels is an array list of Strings containing all the labels of an object
     */
    public ArrayList<String> getPossibleLabels(){
        ArrayList<String> possibleLabels = new ArrayList<String>();

        for(FirebaseVisionImageLabel label : labels){
            possibleLabels.add(label.getText().toLowerCase());
        }

        return possibleLabels;
    }

    public HashSet<String> getPossibleLabelsHashSet(){
        return new HashSet<>(getPossibleLabels());
    }

    public Rect getLocation() {
        return location;
    }
}
