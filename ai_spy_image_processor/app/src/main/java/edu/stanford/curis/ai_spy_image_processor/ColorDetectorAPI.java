package edu.stanford.curis.ai_spy_image_processor;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import androidx.palette.graphics.Palette;

import android.content.Context;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.io.*;


/**
 * This api is used to extract the name of the primary color of an object. The color name will be one of the most common colors that children would recognize
 */
public class ColorDetectorAPI {
    private String color;
    private Context thisContent;


    /**
     * Constructor for a ColorDetectorAPI object. Instantiating this object gives access to the getColor() method which returns an image's primary color.
     * @param image is the bitmap image that this api will detect the primar color from
     */
    public ColorDetectorAPI(Bitmap image, Context thisContent){
        this.thisContent = thisContent;
        Palette p = Palette.from(image).generate();
        int dominantColorRgb = p.getDominantColor(0);
        int vibrantColorRgb = p.getVibrantColor(0);
        String dominantColorName = getColorNameFromRGB(getRGB(dominantColorRgb));
        this.color = dominantColorName;
    }

    public String getColor() {
        return color;
    }

    //inspired from https://stackoverflow.com/questions/2262100/rgb-int-to-rgb-python
    private int[] getRGB (int rgbNum){
        int[] rgb = new int[3];

        rgb[0] = (rgbNum >> 16) & 255;
        rgb[1] = (rgbNum >> 8) & 255;
        rgb[2] = rgbNum & 255;

        return rgb;
    }

    //Creates an ArrayList of the common colors that children will be likely to guess
    private ArrayList<ColorName> initColorList() {
        ArrayList<ColorName> colorList = new ArrayList<ColorName>();
        //read training data in assets folder
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(this.thisContent.getAssets().open("training.txt")));
            String strLine;
            //Read File Line By Line
            while ((strLine = reader.readLine()) != null)   {
                // Print the content on the console
                System.out.println(strLine);
                String[] res = strLine.split("[,]", 0);
                int r = Integer.parseInt(res[0]);
                int g = Integer.parseInt(res[1]);
                int b = Integer.parseInt(res[2]);
                colorList.add(new ColorName(res[3], r, g, b,0));
            }
        } catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        /*
        colorList.add(new ColorName("white", 0xFF, 0xFF, 0xFF));
        */

        return colorList;
    }
    //*********************************ORIGINAL FUNCTION***************************************
    //This old function works by finding and returning the string name  of the common colors has the smallest difference in rgb value to the object color
    //Inspired from https://cindyxiaoxiaoli.wordpress.com/2014/02/15/convert-an-rgb-valuehex-valuejava-color-object-to-a-color-name-in-java/
    /*
    private String getColorNameFromRgb(int[] rgb) {
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];
        ArrayList<ColorName> colorList = initColorList();
        Stack<ColorName> topColors = new Stack<>();

        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
                topColors.push(c);
            }
        }

        if (closestMatch != null) { //TODO: maybe instead of returning one color, return the top 3 from topColors as long as the mse < 3,000 (have a primary, secondary, and tertiary option sense color is subjective)
            return closestMatch.getName();
        } else {
            return "No matched color name.";
        }
    }
    */
    //New function that using kNN technique to detect color.
    private String getColorNameFromRGB(int[] rgb) {
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];
        ArrayList<ColorName> colorList = initColorList();

        ColorName closestMatch1 = null;
        ColorName closestMatch2 = null;
        ColorName closestMatch3 = null;

        double min1 = Integer.MAX_VALUE;
        double min2 = Integer.MAX_VALUE;
        double min3 = Integer.MAX_VALUE;

        double distance;
        for (ColorName c : colorList) {
            distance = c.calculateDistance(r, g, b);
            if (distance < min1) {
                min3 = min2;
                min2 = min1;
                min1 = distance;
                closestMatch1 = c;
                closestMatch1.setDistance(distance);
            } else if (distance < min2) {
                min3 = min2;
                min2 = distance;
                closestMatch2 = c;
                closestMatch2.setDistance(distance);

            } else if (distance < min3) {
                min3 = distance;
                closestMatch3 = c;
                closestMatch3.setDistance(distance);

            }
        }

        //TODO:Handle null object reference exceptions (closestMatch objects might be null)
        if(closestMatch1.getName().equalsIgnoreCase(closestMatch2.getName())) {
            return closestMatch1.getName();
        } else if(closestMatch1.getName().equalsIgnoreCase(closestMatch3.getName())) {
            return closestMatch1.getName();
        } else if(closestMatch2.getName().equalsIgnoreCase(closestMatch3.getName())) {
            return closestMatch2.getName();
        } else {
            return "Not confident!";
        }
    }

    //This inner class is used to encapsulate information about a color, including its string name
    public class ColorName {
        private double distance;
        public int r, g, b;
        public String name;

        public ColorName(String name, int r, int g, int b, double distance) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
            this.distance = 0;
        }
       /*
        public int computeMSE(int pixR, int pixG, int pixB) {
            return (int) (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b)) / 3);
        }
        */
        public double calculateDistance(int pixR, int pixG, int pixB) {
            double distance = ((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b));
            return Math.sqrt(distance);
        }

        public String getName() {
            return name;
        }
        public void setDistance(double distance) {
            this.distance = distance;
        }
    }

}
