package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.tasks.Tasks.await;

public class LabelDetectionAPI {

    public static List<FirebaseVisionImageLabel> getImageLabels(Context content, String imagePath){
        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(content, Uri.fromFile(new File(imagePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getCloudImageLabeler();

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
