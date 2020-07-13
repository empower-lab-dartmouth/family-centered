package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.widget.ImageView;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.ColorInfo;
import com.google.api.services.vision.v1.model.DominantColorsAnnotation;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageProperties;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.mlkit.vision.objects.DetectedObject;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;



/*
Code and design based off: https://github.com/Thumar/GoogleVisionAPI by Thumar.
 */

public class DisplayImageActivity extends BasicFunctionality implements AdapterView.OnItemSelectedListener {
    private ImageView imageView;
    private Bitmap bitmap;
    private Feature feature;
    private Spinner spinnerVisionAPI;
    private TextView visionAPIData;


    private String[] visionAPI = new String[]{"LABEL_DETECTION","LANDMARK_DETECTION", "LOGO_DETECTION", "SAFE_SEARCH_DETECTION", "IMAGE_PROPERTIES"};
    private String api = visionAPI[0];

    private static final String CLOUD_VISION_API_KEY = "AIzaSyBrjvW-v6XkEC6XxO_GarOZbxfalDwfzvc";

    private static final String TAG = "API Activity";

    public static final String SERVER_PICTURE_POST = "http://10.0.2.2:3000/label_picture";

    private String imagePath;




    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image_layout);
        imageView = findViewById(R.id.imageView);
        Bitmap picture = BitmapFactory.decodeFile(getIntent().getStringExtra("image_path"));
        imageView.setImageBitmap(picture);

        imagePath = getIntent().getStringExtra("image_path");

        visionAPIData = findViewById(R.id.visionApiText);
        visionAPIData.setText("Processing...");


        ButterKnife.bind(this);
        feature = new Feature();
        bitmap = picture;
        feature.setType(visionAPI[0]);
        feature.setMaxResults(10);

        spinnerVisionAPI = findViewById(R.id.spinnerVisionApi);

        spinnerVisionAPI.setOnItemSelectedListener(this);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, visionAPI);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVisionAPI.setAdapter(dataAdapter);

        Context thisContent = this.getApplicationContext();


        //Asynchronously run APIs to collect data about the image
        new AsyncTask<Object, Void, ArrayList<AISpyObject>>() {
            @Override
            protected ArrayList<AISpyObject> doInBackground(Object... params) { //TODO: Once all apis are implemented, this should return the full image data structure that we want to build (A map of colors to objects)
                ArrayList<Bitmap> croppedObjects;
                ArrayList<DetectedObject> detectedObjects;
                ArrayList<AISpyObject> aiSpyObjects = new ArrayList<>();


                String newFilePath = "";
                try {

                    //This api uses Firebase ML Kit to locate objects in the image and returns their boundary boxes
//                    objectBoundaryBoxes = ObjectDetectionAPI.getObjectBoundaryBoxes(thisContent, imagePath);
                    detectedObjects = (ArrayList<DetectedObject>) ObjectDetectionAPI.getObjectBoundaryBoxes(thisContent, imagePath);

                    //This api takes an image and boundary boxes and returns the a cropped bitmap for each boundary box
//                    croppedObjects = ObjectCropperAPI.getCroppedObjects(objectBoundaryBoxes, imagePath);
                    croppedObjects = ObjectCropperAPI.getCroppedObjects(detectedObjects, imagePath);

                    //Store cropped bitmaps in files
                    for (Bitmap croppedObject: croppedObjects){

                        //Find the dominant color in the object
                        String color = new ColorDetectorAPI(croppedObject).getColor();

                        //Save the object image
                        newFilePath = createNewImageFile();
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
                        ArrayList<FirebaseVisionImageLabel> labels = new ArrayList<>(LabelDetectionAPI.getImageLabels(thisContent, newFilePath));

                        //Store object info in AISpyObject
                        AISpyObject aiSpyObject = new AISpyObject(croppedObject, newFilePath, labels, color);
                        aiSpyObjects.add(aiSpyObject);

                    }

                }catch (Exception e) {
                    e.printStackTrace();
                }

                return aiSpyObjects;
            }

            //Add the cropped object images to the screen with their corresponding labels
            protected void onPostExecute(ArrayList<AISpyObject> aiSpyObjects) {

                ImageView[] objectImages = {findViewById(R.id.objectView1), findViewById(R.id.objectView2), findViewById(R.id.objectView3), findViewById(R.id.objectView4), findViewById(R.id.objectView5), findViewById(R.id.objectView6)};
                TextView[] objectText = {findViewById(R.id.objectText1), findViewById(R.id.objectText2), findViewById(R.id.objectText3), findViewById(R.id.objectText4), findViewById(R.id.objectText5), findViewById(R.id.objectText6)};

                for (int i = 0; i < aiSpyObjects.size() && i < objectImages.length; i++){
                    objectImages[i].setImageBitmap(aiSpyObjects.get(i).getImage());
                    objectText[i].setText(aiSpyObjects.get(i).getColor() + "\n\n" + aiSpyObjects.get(i).getLabelsText());
                }

            }
        }.execute();

    }

    private String createNewImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String newImagePath = image.getAbsolutePath();

        return newImagePath;
    }



    public void callCloudVision(final Bitmap bitmap, final Feature feature) {
        visionAPIData.setText("Processing...");


        final List<Feature> featureList = new ArrayList<>();
        featureList.add(feature);

        final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();

        AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
        annotateImageReq.setFeatures(featureList);
        annotateImageReq.setImage(getImageEncodeImage(bitmap));
        annotateImageRequests.add(annotateImageReq);



        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(annotateImageRequests);

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);
                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                TextView visionAPIData = findViewById(R.id.visionApiText);

                visionAPIData.setText(result);
            }
        }.execute();

    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        AnnotateImageResponse imageResponses = response.getResponses().get(0);

        List<EntityAnnotation> entityAnnotations;

        String message = "";
        switch (api) {
            case "LANDMARK_DETECTION":
                entityAnnotations = imageResponses.getLandmarkAnnotations();
                message = formatAnnotation(entityAnnotations);
                break;
            case "LOGO_DETECTION":
                entityAnnotations = imageResponses.getLogoAnnotations();
                message = formatAnnotation(entityAnnotations);
                break;
            case "SAFE_SEARCH_DETECTION":
                SafeSearchAnnotation annotation = imageResponses.getSafeSearchAnnotation();
                message = getImageAnnotation(annotation);
                break;
            case "IMAGE_PROPERTIES":
                ImageProperties imageProperties = imageResponses.getImagePropertiesAnnotation();
                message = getImageProperty(imageProperties);
                break;
            case "LABEL_DETECTION":
                entityAnnotations = imageResponses.getLabelAnnotations();
                message = formatAnnotation(entityAnnotations);
                break;
            case "OBJECT_LOCALIZATION":
//                   message = detectLocalizedObjects(LOA);
                break;
        }
        return message;
    }

    private String formatAnnotation(List<EntityAnnotation> entityAnnotation) {
        String message = "";

        if (entityAnnotation != null) {
            for (EntityAnnotation entity : entityAnnotation) {
                message = message + "    " + entity.getDescription() + " " + entity.getScore() + " " + entity.getLocale() + " " + entity.getLocations();
                message += "\n";
            }
        } else {
            message = "Nothing Found";
        }
        return message;
    }

    private Image getImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        // Convert the bitmap to a JPEG
        // Just in case it's a format that Android understands but Cloud Vision
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Base64 encode the JPEG
        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }

    private String getStringImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        // Convert the bitmap to a JPEG
        // Just in case it's a format that Android understands but Cloud Vision
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        String imageData = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return imageData;
    }


    private String getImageAnnotation(SafeSearchAnnotation annotation) {
        return String.format("adult: %s\nmedical: %s\nspoofed: %s\nviolence: %s\n",
                annotation.getAdult(),
                annotation.getMedical(),
                annotation.getSpoof(),
                annotation.getViolence());
    }

    private String getImageProperty(ImageProperties imageProperties) {
        String message = "";
        DominantColorsAnnotation colors = imageProperties.getDominantColors();
        for (ColorInfo color : colors.getColors()) {
            message = message + "" + color.getPixelFraction() + " - " + color.getColor().getRed() + " - " + color.getColor().getGreen() + " - " + color.getColor().getBlue();
            message = message + "\n";
        }
        return message;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        api = (String) adapterView.getItemAtPosition(i);
        feature.setType(api);
//        api = "OBJECT_LOCALIZATION";
//        feature.setType("OBJECT_LOCALIZATION");
        if (bitmap != null){

//            new AsyncTask<Object, Void, String>() {
//                @Override
//                protected String doInBackground(Object... params) {
//                    try {
//                        callCloudVision(bitmap, feature);
//                    } catch(Exception e) {
//                        Log.d(TAG, "failed to make API request because " + e);
//                    }
//
//                    return "done";
//                }
//
//                protected void onPostExecute(String result) {
//                    System.out.println(result);
//                }
//
//            }.execute();

            callCloudVision(bitmap, feature);
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

//    public void detectLocalizedObjects(String filePath, PrintStream out)
//            throws Exception, IOException {
//        List<AnnotateImageRequest> requests = new ArrayList<>();
//
//        final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();
//
//        AnnotateImageRequest request = new AnnotateImageRequest();
//
//        request.addFeatures(featureList);
//        annotateImageReq.setImage(getImageEncodeImage(bitmap));
//
//        requests.add(request);
//
//        AnnotateImageRequest request =
//                AnnotateImageRequest.newBuilder()
//                        .addFeatures(Feature.newBuilder().setType(Type.OBJECT_LOCALIZATION))
//                        .setImage(getImageEncodeImage(bitmap))
//                        .build();
//        requests.add(request);
//
//        // Perform the request
//        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
//            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
//            List<AnnotateImageResponse> responses = response.getResponsesList();
//
//            // Display the results
//            for (AnnotateImageResponse res : responses) {
//                for (LocalizedObjectAnnotation entity : res.getLocalizedObjectAnnotationsList()) {
//                    out.format("Object name: %s\n", entity.getName());
//                    out.format("Confidence: %s\n", entity.getScore());
//                    out.format("Normalized Vertices:\n");
//                    entity
//                            .getBoundingPoly()
//                            .getNormalizedVerticesList()
//                            .forEach(vertex -> out.format("- (%s, %s)\n", vertex.getX(), vertex.getY()));
//                }
//            }
//        }


}
