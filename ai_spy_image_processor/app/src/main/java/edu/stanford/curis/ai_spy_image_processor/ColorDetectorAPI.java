package edu.stanford.curis.ai_spy_image_processor;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import androidx.palette.graphics.Palette;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.ColorInfo;
import com.google.api.services.vision.v1.model.DominantColorsAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageProperties;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.*;

import org.tensorflow.lite.Interpreter;


/**
 * This api is used to extract the name of the primary color of an object. The color name will be one of the most common colors that children would recognize
 * This API is inspired by Google Cloud Vision sample for Android
 * https://github.com/GoogleCloudPlatform/cloud-vision/blob/master/android/CloudVision/app/src/main/java/com/google/sample/cloudvision/MainActivity.java
 */
public class ColorDetectorAPI {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyB3V8G7LLqTIMJ0m9xfpUELqsZwM9yrxYM";
    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<AISpyObject> returnList;
    private Context thisContent;
    private String[] colorLabels = new String[]{ "red", "green", "blue", "orange", "yellow", "pink", "purple", "brown", "grey", "black"};

    /**
     * Constructor for a ColorDetectorAPI object. Instantiating this object gives access to the getColor() method which returns an image's primary color.
     * @param image is the bitmap image that this api will detect the primary color from
     */
    public ColorDetectorAPI(ArrayList<AISpyObject> objectList, Context thisContent){
        this.thisContent = thisContent;
        /*
        Palette p = Palette
                .from(image)
                .maximumColorCount(20)
                .generate();
        int dominantColorRgb = p.getDominantColor(0);
         */
        this.returnList = callCloudVision(objectList);

    }

    private static float[] getImageProperty(ImageProperties imageProperties) {
        //String message = "";
        DominantColorsAnnotation colors = imageProperties.getDominantColors();
        ColorInfo dominant = colors.getColors().get(0);
        float[] rgbValue = new float[]{dominant.getColor().getRed(), dominant.getColor().getGreen(), dominant.getColor().getBlue()};

        return rgbValue;
    }

    private ArrayList<AISpyObject> callCloudVision(ArrayList<AISpyObject> objectList) {
        try {
            Vision.Images.Annotate mRequest = prepareAnnotationRequest(objectList);
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();

                //Get responses
                int i = 0;
                for(AISpyObject object : objectList) {
                    ImageProperties imageProperties = response.getResponses().get(i).getImagePropertiesAnnotation();
                    float[] rgb = getImageProperty(imageProperties);
                    if(rgb == null) {
                        object.setColor("Not available!");
                    } else {
                        object.setColor(getColorNameFromRGBUsingMLModelCloudVision(rgb));
                    }
                    i++;
                }

                return objectList;

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            //return "Cloud Vision API request failed. Check logs for details.";

        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
        return null;
    }

    private Vision.Images.Annotate prepareAnnotationRequest(ArrayList<AISpyObject> objectList) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    /*
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }

                    */
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();

        ArrayList<Feature> feature = new ArrayList<Feature>() {{
            Feature imageProperties = new Feature();
            imageProperties.setType("IMAGE_PROPERTIES");
            add(imageProperties);
        }};

        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            for(AISpyObject object : objectList) {
                AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
                annotateImageRequest.setImage(getImageEncodeImage(object.getImageForColorDetection()));
                // add the features we want
                annotateImageRequest.setFeatures(feature);
                // Add the list of one thing to the request
                add(annotateImageRequest);
            }
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private Image getImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        // Convert the bitmap to a PNG
        // Just in case it's a format that Android understands but Cloud Vision
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        // Base64 encode the PNG
        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }


    public ArrayList<AISpyObject> getReturnList() {
        return this.returnList;
    }

    /*OLD VERSION USING PALLETE
    private String getColorNameFromRGBUsingMLModel(int rgb){
        float[][] outputs = new float[1][10];
        try (Interpreter tflite = new Interpreter(loadModelFile(thisContent))) {
            float[][] inputRGB = makeRGBInputTensor(rgb);
            tflite.run(inputRGB, outputs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i = argMax(outputs);
        String color =  colorLabels[i];
        return color;
    }

     */

    private String getColorNameFromRGBUsingMLModelCloudVision(float[] rgb){
        float[][] outputs = new float[1][10];
        try (Interpreter tflite = new Interpreter(loadModelFile(thisContent))) {
            float[][] inputRGB = makeRGBInputTensorCloudVision(rgb);
            tflite.run(inputRGB, outputs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i = argMax(outputs);
        String color =  colorLabels[i];
        return color;
    }

    private int argMax(float[][] outputs){
        float max = 0;
        int maxIndex = -1;
        for (int i = 0; i < outputs[0].length; i++){
            if (outputs[0][i] > max){
                max = outputs[0][i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        String MODEL_ASSETS_PATH = "model.tflite";
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_ASSETS_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /* OLD VERSION USING PALETTE
    private float[][] makeRGBInputTensor(int rgbNum){
        float[] rgb = new float[3];

        float r = (float) ((rgbNum >> 16) & 255);
        float g = (float) ((rgbNum >> 8) & 255);
        float b = (float) (rgbNum & 255);

        rgb[0] = r / 255;
        rgb[1] = g / 255;
        rgb[2] = b / 255;

        float[][] tensor = new float[1][3];
        tensor[0] = rgb;

        return tensor;
    }
    */
    private float[][] makeRGBInputTensorCloudVision(float[] rgbValues){
        float[] rgb = rgbValues;

        rgb[0] = rgb[0] / 255;
        rgb[1] = rgb[1] / 255;
        rgb[2] = rgb[2] / 255;

        float[][] tensor = new float[1][3];
        tensor[0] = rgb;

        return tensor;
    }

}
