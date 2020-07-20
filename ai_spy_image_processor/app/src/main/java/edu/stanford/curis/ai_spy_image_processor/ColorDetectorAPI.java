package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;

import androidx.palette.graphics.Palette;

import java.util.ArrayList;
import java.util.Stack;

/*
This api is used to extract the name of the primary color of an object. The color name will be one of the most common colors that children would recognize
 */

public class ColorDetectorAPI {
    private String color;


    public ColorDetectorAPI(Bitmap image){
        Palette p = Palette.from(image).generate();
        int dominantColorRgb = p.getDominantColor(0);
        int vibrantColorRgb = p.getVibrantColor(0);
        String dominantColorName = getColorNameFromRgb(getRGB(dominantColorRgb));
//        String vibrantColorName = getColorNameFromRgb(getRGB(vibrantColorRgb));

//        this.color = vibrantColorRgb != 0 ? vibrantColorName : dominantColorName;
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
        colorList.add(new ColorName("white", 0xFF, 0xFF, 0xFF));
        colorList.add(new ColorName("white", 0xE0, 0xEA, 0xE0));
        colorList.add(new ColorName("white", 0xA8, 0xB8, 0xB8));

        colorList.add(new ColorName("black", 0x00, 0x00, 0x00));
        colorList.add(new ColorName("black", 0x20, 0x20, 0x20));

        colorList.add(new ColorName("grey", 0x80, 0x80, 0x80));

        //Red colors
        colorList.add(new ColorName("red", 0xFF, 0x00, 0x00));
        colorList.add(new ColorName("red", 0xFF, 0x66, 0x66));
        colorList.add(new ColorName("red", 0x99, 0x00, 0x00));
        colorList.add(new ColorName("red", 0x40, 0x00, 0x00));
        colorList.add(new ColorName("red", 0x98, 0x29, 0x30));

        //Blue colors
        colorList.add(new ColorName("blue", 0x00, 0x00, 0xFF));
        colorList.add(new ColorName("blue", 0x66, 0x66, 0xFF));
        colorList.add(new ColorName("blue", 0x00, 0x00, 0x99));
        colorList.add(new ColorName("blue", 0x00, 0xFF, 0xFF));
        colorList.add(new ColorName("blue", 0x99, 0xFF, 0xFF));
        colorList.add(new ColorName("blue", 0x00, 0x08, 0xFF));
        colorList.add(new ColorName("blue", 0x66, 0xB2, 0xFF));
        colorList.add(new ColorName("blue", 0x76, 0xA6, 0x9C));

        //Green colors
        colorList.add(new ColorName("green", 0x00, 0xFF, 0x00));
        colorList.add(new ColorName("green", 0xF5, 0x80, 0x00));
        colorList.add(new ColorName("green", 0xF5, 0x80, 0x00));
        colorList.add(new ColorName("green", 0x00, 0x99, 0x00));
        colorList.add(new ColorName("green", 0x99, 0xFF, 0x99));
        colorList.add(new ColorName("green", 0xB2, 0xFF, 0x66));
        colorList.add(new ColorName("green", 0x80, 0xB0, 0x20));
        colorList.add(new ColorName("green", 0x00, 0x48, 0x28));


        //Yellow colors
        colorList.add(new ColorName("yellow", 0xFF, 0xFF, 0x00));
        colorList.add(new ColorName("yellow", 0xFF, 0xFF, 0xC4));
        colorList.add(new ColorName("yellow", 0xFF, 0xFF, 0x99));
        colorList.add(new ColorName("yellow", 0xCC, 0xCC, 0x00));
        colorList.add(new ColorName("yellow", 0xF0, 0xD0, 0x58));

        //Purple colors
        colorList.add(new ColorName("purple", 0x80, 0x00, 0x80));
        colorList.add(new ColorName("purple", 0xFF, 0x00, 0xFF));
        colorList.add(new ColorName("purple", 0x66, 0x00, 0x66));
        colorList.add(new ColorName("purple", 0x4C, 0x00, 0x99));
        colorList.add(new ColorName("purple", 0xB2, 0x66, 0xFF));

        //Brown colors
        colorList.add(new ColorName("brown", 0xCC, 0x66, 0x00));
        colorList.add(new ColorName("brown", 0x99, 0x4C, 0x00));
        colorList.add(new ColorName("brown", 0xCC, 0x66, 0x00));
        colorList.add(new ColorName("brown", 0xFF, 0xB2, 0x66));
        colorList.add(new ColorName("brown", 0xB0, 0x88, 0x30));

        //Orange colors
        colorList.add(new ColorName("orange", 0xFF, 0xA5, 0x00));

        //Pink colors
        colorList.add(new ColorName("pink", 0xFF, 0xC0, 0xCB));





        return colorList;
    }

    //This function works by finding and returning the string name  of the common colors has the smallest difference in rgb value to the object color
    //Inspired from https://cindyxiaoxiaoli.wordpress.com/2014/02/15/convert-an-rgb-valuehex-valuejava-color-object-to-a-color-name-in-java/
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



    //This inner class is used to encapsulate information about a color, including its string name
    public class ColorName {
        public int r, g, b;
        public String name;

        public ColorName(String name, int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
        }

        public int computeMSE(int pixR, int pixG, int pixB) {
            return (int) (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b)) / 3);
        }

        public String getName() {
            return name;
        }
    }

}
