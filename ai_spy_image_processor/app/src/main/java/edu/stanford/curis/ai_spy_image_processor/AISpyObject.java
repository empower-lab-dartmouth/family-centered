package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;


import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

/*
An AISpyObject contains the image wherein that object is located, the primary color of that object, and an ArrayList of the labels firebase generated to describe that object
 */
public class AISpyObject implements Serializable {
    private String imageFileName;
    private Bitmap image;
    private ArrayList<FirebaseVisionImageLabel> labels;
    private String color;
    private Rect location;

    public AISpyObject(Bitmap image, String imageFileName, Rect location, ArrayList<FirebaseVisionImageLabel> labels, String color){
        this.image = image;
        this.imageFileName = imageFileName;
        this.location = location;
        this.labels = labels;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public Bitmap getImage(){
        return this.image;
    }

    public String getLabelsText(){
        String labelsText = "";

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        for (int i = 0; i < 6 && i < labels.size(); i++){ //Stopping at a max of 6 labels just because I don't want the UI to get filled with too many labels
            labelsText += (labels.get(i).getText() + " " + decimalFormat.format((labels.get(i).getConfidence())) + "\n");
        }

        return labelsText;
    }

    public ArrayList<String> getPossibleLabels(){
        ArrayList<String> possibleLabels = new ArrayList<String>();

        for(FirebaseVisionImageLabel label : labels){
            possibleLabels.add(label.getText().toUpperCase());
        }

        return possibleLabels;
    }

    public Rect getLocation() {
        return location;
    }
}
