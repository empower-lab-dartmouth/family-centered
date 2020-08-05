package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Environment;

import com.google.mlkit.vision.objects.DetectedObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This api takes an image and boundary boxes and returns the a cropped bitmap for each boundary box
 */
public class BitmapAPI {


    public static Bitmap getCroppedObject(DetectedObject detectedObject, String imagePath){
//        Bitmap picture = BitmapFactory.decodeFile(imagePath);
//
//        //correct Orientation
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90);
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, picture.getWidth(), picture.getHeight(), true);
//        picture = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        Bitmap picture = getCorrectOrientation(imagePath);

        Rect boundaryBox = detectedObject.getBoundingBox();
        int left = boundaryBox.left;
        int top = boundaryBox.top;
        int width = boundaryBox.width();
        int height = boundaryBox.height();

        int origWidth = picture.getWidth();
        int origHeight = picture.getHeight();

        if ((top + height) > origHeight) height = origHeight - top;
        if (width > origWidth) width = origWidth - left;

        System.out.println(left + top + width + height + origWidth + origHeight);
        return (Bitmap.createBitmap(picture, left, top, width, height));
    }

    public static Bitmap getCorrectOrientation(String imagePath){
        try {
            Bitmap picture = BitmapFactory.decodeFile(imagePath);
            ExifInterface exif = new ExifInterface(imagePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0) {matrix.preRotate(rotationInDegrees);}
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, picture.getWidth(), picture.getHeight(), true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            return rotatedBitmap;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }


}