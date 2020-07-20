package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;

import com.google.mlkit.vision.objects.DetectedObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
This api takes an image and boundary boxes and returns the a cropped bitmap for each boundary box
 */
public class ObjectCropperAPI {


    public static Bitmap getCroppedObject(DetectedObject detectedObject, String imagePath){
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);

        Rect boundaryBox = detectedObject.getBoundingBox();
        return (Bitmap.createBitmap(originalBitmap, boundaryBox.left, boundaryBox.top, boundaryBox.width(), boundaryBox.height()));
    }


}
