package edu.stanford.curis.ai_spy_image_processor;

import android.graphics.Bitmap;

import androidx.palette.graphics.Palette;

import java.util.ArrayList;

//Thanks to https://cindyxiaoxiaoli.wordpress.com/2014/02/15/convert-an-rgb-valuehex-valuejava-color-object-to-a-color-name-in-java/

public class ColorDetectorAPI {
    private String color;


    public ColorDetectorAPI(Bitmap image){
        Palette p = Palette.from(image).generate();
        int dominantColorRgb = p.getDominantColor(0);

        //https://stackoverflow.com/questions/2262100/rgb-int-to-rgb-python
        int r = (dominantColorRgb >> 16) & 255;
        int g = (dominantColorRgb >> 8) & 255;
        int b = dominantColorRgb & 255;

        ArrayList<ColorName> commonColors = initColorList();

        this.color = getColorNameFromRgb(r, g, b);
    }

    public String getColor() {
        return color;
    }

    //Creates an ArrayList of the common colors that children will be likely to guess
    private ArrayList<ColorName> initColorList() {
        ArrayList<ColorName> colorList = new ArrayList<ColorName>();
        colorList.add(new ColorName("white", 0xFF, 0xFF, 0xFF));
        colorList.add(new ColorName("black", 0x00, 0x00, 0x00));
        colorList.add(new ColorName("red", 0xFF, 0x00, 0x00));
        colorList.add(new ColorName("blue", 0x00, 0x00, 0xFF));
        colorList.add(new ColorName("green", 0xF5, 0x80, 0x00));
        colorList.add(new ColorName("yellow", 0xFF, 0xFF, 0xC4));
        colorList.add(new ColorName("purple", 0x80, 0x00, 0x80));
        colorList.add(new ColorName("pink", 0xFF, 0xC0, 0xCB));
        colorList.add(new ColorName("brown", 0xCC, 0x66, 0x00));
        colorList.add(new ColorName("orange", 0x00, 0x00, 0x00));
        colorList.add(new ColorName("grey", 0x80, 0x80, 0x80));
        return colorList;
    }

    //This function works by finding and returning the string name  of the common colors has the smallest difference in rgb value to the object color
    private String getColorNameFromRgb(int r, int g, int b) {
        ArrayList<ColorName> colorList = initColorList();
        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
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
