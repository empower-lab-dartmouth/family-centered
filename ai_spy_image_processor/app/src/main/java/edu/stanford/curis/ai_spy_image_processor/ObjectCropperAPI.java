package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ObjectCropperAPI {

    public static ArrayList<Bitmap> getCroppedObjects(ArrayList<Rect> objectBoundaryBoxes, String imagePath){
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
        ArrayList<Bitmap> croppedObjects = new ArrayList<Bitmap>();

        for (Rect boundaryBox : objectBoundaryBoxes){
            croppedObjects.add(Bitmap.createBitmap(originalBitmap, boundaryBox.left, boundaryBox.top, boundaryBox.width(), boundaryBox.height()));
        }

        return croppedObjects;

    }


}
