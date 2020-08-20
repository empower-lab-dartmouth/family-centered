package projects.android.aispy;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.palette.graphics.Palette;

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

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


/**
 * This api is used to extract the name of the primary color of an object. The color name will be one of the most common colors that children would recognize
 * This API is inspired by Google Cloud Vision sample for Android
 * https://github.com/GoogleCloudPlatform/cloud-vision/blob/master/android/CloudVision/app/src/main/java/com/google/sample/cloudvision/MainActivity.java
 */
public class ColorDetectorAPI {
    //This API_KEY will be valid through July 2021.
    private static final String CLOUD_VISION_API_KEY = "AIzaSyB3V8G7LLqTIMJ0m9xfpUELqsZwM9yrxYM";
    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<AISpyObject> returnList;
    private Context thisContent;
    private boolean status;
    private String[] colorLabels = new String[]{ "red", "green", "blue", "orange", "yellow", "pink", "purple", "brown", "grey", "black"};

    /**
     * Constructor for a ColorDetectorAPI object. Take inputs as an array of the objects whose colors need to be determined. Instantiating this object with the found returnList.
     * @param objectList is the list of objects that needs to be determined for color.
     * @param thisContent: context
     */
    public ColorDetectorAPI(ArrayList<AISpyObject> objectList, Context thisContent){
        this.thisContent = thisContent;
        this.status = true;
        //Getting dominant RGBs by calling Cloud Vision API and then using NN model to detect their names.
        this.returnList = colorNamingTask(objectList);
        // If Cloud Vision does not work, get RGBs values using Pallete class.
        if (!status) {
            for (AISpyObject object : objectList) {
                Palette p = Palette
                        .from(object.getImage())
                        .maximumColorCount(20)
                        .generate();
                int dominantColorRgb = p.getDominantColor(0);
                object.setColor(getColorNameFromRGBUsingMLModel(makeRGBInputTensor(dominantColorRgb)));
            }
            this.returnList = objectList;
        }
    }

    /**
     * get Method
     * @return the returnList
     */
    public ArrayList<AISpyObject> getReturnList() {
        return this.returnList;
    }

    /**
     * used to extract the dominant RGB from cloud's response
     * @param imageProperties: get the RGB value from Cloud Vision's response
     * @return RGB values
     */
    private static float[] getImageProperty(ImageProperties imageProperties) {
        DominantColorsAnnotation colors = imageProperties.getDominantColors();
        //get the most dominant color
        ColorInfo dominant = colors.getColors().get(0);
        float[] rgbValue = new float[]{dominant.getColor().getRed(), dominant.getColor().getGreen(), dominant.getColor().getBlue()};
        return rgbValue;
    }

    /**
     * This function is used to get response from Cloud and naming colors based on RGB values
     * @param objectList: list of detected objects
     * @return new list with colors added
     */
    private ArrayList<AISpyObject> colorNamingTask(ArrayList<AISpyObject> objectList) {
        //Get responses
        BatchAnnotateImagesResponse response = callCloudVision(objectList);
        if (response != null) {
            int i = 0;
            for (AISpyObject object : objectList) {
                Log.d(TAG, "Got Response!!!!!!!!!!!!!!!!!");
                //get response for each object
                ImageProperties imageProperties = response.getResponses().get(i).getImagePropertiesAnnotation();
                float[] rgb = getImageProperty(imageProperties);
                if (rgb == null) {
                    object.setColor("Not available!");
                } else {
                    //calling other function to name the RGB value as a color and set object's color attribute corresponding.
                    object.setColor(getColorNameFromRGBUsingMLModel(makeRGBInputTensor(rgb)));
                }
                i++;
            }
            return objectList;
        }
        this.status = false;
        return null;
    }

    /**
     * Method to make a request batch and get responses from Cloud Vision and detect the names of color.
     * @param objectList: the list of objects whose colors need to be determined.
     * @return an updated list that have been added the color labels.
     */
    private BatchAnnotateImagesResponse callCloudVision(ArrayList<AISpyObject> objectList) {
        try {
            //prepare request
            Vision.Images.Annotate mRequest = prepareAnnotationRequest(objectList);
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                //execute the request
                BatchAnnotateImagesResponse response = mRequest.execute();
                //Get responses
                return response;
            } catch (GoogleJsonResponseException e) {
                this.status = false;
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                this.status = false;
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
        } catch (IOException e) {
            this.status = false;
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
        this.status = false;
        return null;
    }

    /**
     * preparing the request batch
     * @param objectList: the list of objects whose colors need to be determined.
     * @return a ready request batch to execute in other function
     * @throws IOException
     */
    private Vision.Images.Annotate prepareAnnotationRequest(ArrayList<AISpyObject> objectList) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        //The current API key is not restricted that's why the override part is commented out
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

        //initialize a new batch of requests
        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        //setting the requesting feature
        ArrayList<Feature> feature = new ArrayList<Feature>() {{
            Feature imageProperties = new Feature();
            imageProperties.setType("IMAGE_PROPERTIES");
            add(imageProperties);
        }};
        //go through the objectList and add each one to the request batch
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            for(AISpyObject object : objectList) {
                AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
                annotateImageRequest.setImage(getImageEncodeImage(object.getImageForColorDetection()));
                // add the features we want
                annotateImageRequest.setFeatures(feature);
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

    /**
     * Convert the bitmap to a PNG. Just in case it's a format that Android understands but Cloud Vision
     * @param bitmap: contaning bitmap of the object to be generated to base64EncodedImage
     * @return the base64EncodedImage object
     */
    private Image getImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        final int MAX_SIZE = 25000;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //set a default quality rate at 90
        int currQuality = 90;
        int currSize = 0;
        //keep downgrading/compressing the bitmap until it does not exceed the MAX_SIZE
        do {
            byteArrayOutputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, currQuality, byteArrayOutputStream);
            currSize = byteArrayOutputStream.toByteArray().length;
            currQuality -= 20;
            if(currQuality < 0) {
                break;
            }
        } while (currSize >= MAX_SIZE);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        // Base64 encode the JPEG
        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }

    /**
     * Getting color's name using the RGB value
     * @param inputRGB: tensor containing the RGB input
     * @return the color name using NN model
     */
    private String getColorNameFromRGBUsingMLModel(float[][] inputRGB){
        float[][] outputs = new float[1][10];
        try (Interpreter tflite = new Interpreter(loadModelFile(thisContent))) {
            tflite.run(inputRGB, outputs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i = argMax(outputs);
        String color =  colorLabels[i];
        return color;
    }

    /**
     * return the index of maximum 2D array value
     * @param outputs: 2d array of outputs
     * @return the index of maximum value
     */
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

    /**
     * load Neural Network model file
     * @param context
     * @return MappedByteBuffer
     * @throws IOException
     */
    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        String MODEL_ASSETS_PATH = "model.tflite";
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_ASSETS_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Converting rgb value to a 2d-tensor input
     * @param rgbNum: rgb value geeting from Pallete
     * @return 2d tensor input
     */
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
    /**
     * Converting rgb value to a 2d-tensor input- Overide method
     * @param rgbValues: rgb value geeting from Cloud Vision
     * @return 2d tensor input
     */
    private float[][] makeRGBInputTensor(float[] rgbValues){
        float[] rgb = rgbValues;

        rgb[0] = rgb[0] / 255;
        rgb[1] = rgb[1] / 255;
        rgb[2] = rgb[2] / 255;

        float[][] tensor = new float[1][3];
        tensor[0] = rgb;

        return tensor;
    }

}
