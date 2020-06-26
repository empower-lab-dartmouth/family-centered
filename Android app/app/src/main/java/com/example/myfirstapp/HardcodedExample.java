package com.example.myfirstapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;

import android.graphics.drawable.BitmapDrawable;

public class HardcodedExample extends BasicFunctionality{

    ImageView picture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hardcoded_example);

        picture = (ImageView)findViewById(R.id.hardcodedImage);

        picture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(HardcodedExample.this, "You clicked on ImageView", Toast.LENGTH_LONG).show();

            }
        });

        generateLabels(picture);
    }

    //Thanks https://code.tutsplus.com/tutorials/getting-started-with-firebase-ml-kit-for-android--cms-31305
    public void generateLabels(ImageView v) {
        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector();//LabelDetector;
        detector.detectInImage(FirebaseVisionImage.fromBitmap(
                ((BitmapDrawable) v.getDrawable()).getBitmap()  ));
    }
}
