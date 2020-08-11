package edu.stanford.curis.ai_spy_image_processor;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import androidx.palette.graphics.Palette;

import android.content.Context;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.io.*;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;


/**
 * This api is used to extract the name of the primary color of an object. The color name will be one of the most common colors that children would recognize
 */
public class ColorDetectorAPI {
    private String color;
    private Context thisContent;

    private String[] colorLabels = new String[]{ "red", "green", "blue", "orange", "yellow", "pink", "purple", "brown", "grey", "black"};


    /**
     * Constructor for a ColorDetectorAPI object. Instantiating this object gives access to the getColor() method which returns an image's primary color.
     * @param image is the bitmap image that this api will detect the primar color from
     */
    public ColorDetectorAPI(Bitmap image, Context thisContent){
        this.thisContent = thisContent;
        Palette p = Palette.from(image).generate();
        int dominantColorRgb = p.getDominantColor(0);
        this.color = getColorNameFromRGBUsingMLModel(dominantColorRgb);


    }

    public String getColor() {
        return color;
    }

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

}
