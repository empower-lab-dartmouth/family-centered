package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;


import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;

import java.util.ArrayList;

public class AISpyObject {
    private String imageFileName;
    private Bitmap image;
    private ArrayList<FirebaseVisionImageLabel> labels;
    private String color;

    public AISpyObject(Bitmap image, String imageFileName, ArrayList<FirebaseVisionImageLabel> labels, String color){
        this.image = image;
        this.imageFileName = imageFileName;
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

        for (int i = 0; i < 6 && i < labels.size(); i++){ //Stopping at a max of 6 labels just because I don't want the UI to get filled with too many labels
            labelsText += (labels.get(i).getText() + "\n");
        }

        return labelsText;
    }
}
