package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.android.gms.tasks.Tasks.await;

/*
This api queries Firebase ML Vision kit' label detection feature to get the labels associated with an image
 */
public class LabelDetectionAPI {

    public static List<FirebaseVisionImageLabel> getImageLabels(Context content, String imagePath){
        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(content, Uri.fromFile(new File(imagePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set the minimum confidence required:
         FirebaseVisionCloudImageLabelerOptions options =
             new FirebaseVisionCloudImageLabelerOptions.Builder()
                 .setConfidenceThreshold(0.85f)
                 .build();

        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getCloudImageLabeler(options);

        if (image != null){
            //Pass image to processor and get a list of detected objects
            try {
                List<FirebaseVisionImageLabel> labels = await(labeler.processImage(image));
                return labels;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
