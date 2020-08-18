package projects.android.aispy;



import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static com.google.android.gms.tasks.Tasks.await;

/**
 * This api queries queries Firebase ML Vision kit' object detection feature to locate objects in the image and returns their boundary boxes
 */
public class ObjectDetectionAPI extends Thread {

    private ArrayList<Rect> objectBoundaryBoxes;
    private Context content;
    private String imagePath;
    private CountDownLatch countDownLatch;

    public static List<DetectedObject> getObjectBoundaryBoxes(Context content, String imagePath){

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
            return null;
        }

        //Pass image to processor and get a list of detected objects
        try {
            List<DetectedObject> list = await (objectDetector.process(image));
            return list;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Take the list of detected objects and return only the boundary boxes for those objects
    private static ArrayList<Rect> getObjectInfo(List<DetectedObject> results){
        ArrayList<Rect> objectBoundaryBoxes = new ArrayList<>();

        for (DetectedObject detectedObject : results) {
            Rect boundingBox = detectedObject.getBoundingBox();
            objectBoundaryBoxes.add(boundingBox);
            Integer trackingId = detectedObject.getTrackingId();
            for (DetectedObject.Label label : detectedObject.getLabels()) {
                String text = label.getText();
                System.out.println("***********" + text);
                float confidence = label.getConfidence();
            }
        }

        return objectBoundaryBoxes;
    }

}

