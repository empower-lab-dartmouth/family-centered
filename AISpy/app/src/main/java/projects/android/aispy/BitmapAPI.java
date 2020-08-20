package projects.android.aispy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;

import com.google.mlkit.vision.objects.DetectedObject;

import java.io.IOException;


/**
 * This api takes an image and boundary boxes and returns the a cropped bitmap for each boundary box
 */
public class BitmapAPI {

    /**
     * This function is used to crop the object's bitmap
     * @param detectedObject: detected object
     * @param imagePath: image path
     * @return the cropped object's bitmap
     */
    public static Bitmap getCroppedObject(DetectedObject detectedObject, String imagePath){

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
        Bitmap ret = (Bitmap.createBitmap(picture, left, top, width, height));
        return ret;
    }

    /**
     * get extended bitmap for object to send to ColorDetection
     * @param detectedObject: detected object
     * @param imagePath: image path
     * @return the extended cropped bitmap
     */
    public static Bitmap getBitmapForCloud(DetectedObject detectedObject, String imagePath){

        Bitmap picture = getCorrectOrientation(imagePath);

        Rect boundaryBox = detectedObject.getBoundingBox();
        int left = boundaryBox.left;
        int top = boundaryBox.top;
        int width = boundaryBox.width();
        int height = boundaryBox.height();

        int newLeft = left - (int)(width*0.5);
        int newTop = top - (int)(height*0.5);

        if(newLeft < 0) {
            newLeft = 0;
        }
        if(newTop < 0) {
            newTop = 0;
        }

        int newWidth = width + (left - newLeft) + (int)(width*0.5);
        int newHeight = height + (top - newTop) + (int)(height*0.5);

        int origWidth = picture.getWidth();
        int origHeight = picture.getHeight();

        if(newWidth + newLeft >origWidth) {
            newWidth = origWidth - newLeft;
        }
        if(newTop + newHeight > origHeight) {
            newHeight = origHeight - newTop;
        }

        Bitmap ret = (Bitmap.createBitmap(picture, newLeft, newTop, newWidth, newHeight));
        return ret;
    }

    public static Bitmap getCorrectOrientation(String imagePath){
        try {
            Bitmap picture = BitmapFactory.decodeFile(imagePath);
            ExifInterface exif = new ExifInterface(imagePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0) {matrix.preRotate(rotationInDegrees);}
            Bitmap rotatedBitmap = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);

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
