package edu.stanford.curis.ai_spy_image_processor;



import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ObjectDetectionAPI {
    private String imagePath;

    public ObjectDetectionAPI(Context content, String imagePath){
        this.imagePath = imagePath;

        // Multiple object detection in static images
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();

        ObjectDetector objectDetector = ObjectDetection.getClient(options);

        //Create input image
        InputImage image;
        try {
            image = InputImage.fromFilePath(content, Uri.fromFile(new File(imagePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //Pass image to processor
        objectDetector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<DetectedObject>>() {
                            @Override
                            public void onSuccess(List<DetectedObject> detectedObjects) {
                                // Task completed successfully
                                // ...
                                getObjectInfo(detectedObjects);


                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });

    }

    private void getObjectInfo(List<DetectedObject> results){
        // The list of detected objects contains one item if multiple
// object detection wasn't enabled.
        for (DetectedObject detectedObject : results) {
            Rect boundingBox = detectedObject.getBoundingBox();
            Integer trackingId = detectedObject.getTrackingId();
            for (DetectedObject.Label label : detectedObject.getLabels()) {
                String text = label.getText();
                System.out.println("***********" + text);
                float confidence = label.getConfidence();
            }
        }
    }
}
