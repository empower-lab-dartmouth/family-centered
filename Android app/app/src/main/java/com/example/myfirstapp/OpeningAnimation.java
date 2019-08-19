package com.example.myfirstapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;

//Hardcoded opening animations. Moves two images around the screen in an interesting pattern. Used by MainActivity and IntroSequenceActivity1
public class OpeningAnimation{

    protected static void runAnimations(final ImageView homeImage, final ImageView eggImage, final View button, final Drawable dragon2, int orientation, final ArrayList<Integer> ts) {
        int i = 0;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            final int i1 = i;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    button.setVisibility(View.INVISIBLE);

                    ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(homeImage, "scaleX", 2f);
                    ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(homeImage, "scaleY", 2f);
                    ObjectAnimator rotator = ObjectAnimator.ofFloat(homeImage, "rotation", 0f, 360f);
                    ObjectAnimator translator;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Path path = new Path();
                        path.arcTo(0f, 0f, 1000f, 400f, 270f, -180f, true);
                        translator = ObjectAnimator.ofFloat(homeImage, View.X, View.Y, path);
                    } else {
                        translator = ObjectAnimator.ofFloat(homeImage, "y", 400f);
                    }

                    scaleUpX.setDuration(ts.get(i1));
                    scaleUpY.setDuration(ts.get(i1+1));
                    rotator.setDuration(ts.get(i1+2));
                    translator.setDuration(ts.get(i1+3));

                    AnimatorSet scaleUp = new AnimatorSet();
                    scaleUp.play(scaleUpX).with(scaleUpY).with(rotator).with(translator);
                    scaleUp.start();

                    homeImage.setVisibility(View.VISIBLE);

                    ObjectAnimator eggRotator = ObjectAnimator.ofFloat(eggImage, "rotation", 0f, 360f);

                    eggRotator.setDuration(ts.get(i1+4));
                    eggRotator.start();

                    eggImage.setVisibility(View.VISIBLE);
                }
            }, ts.get(i1+5));

            final int i2 = i1+6;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator eggScaleX = ObjectAnimator.ofFloat(eggImage, "scaleX", 1.5f);
                    ObjectAnimator eggScaleY = ObjectAnimator.ofFloat(eggImage, "scaleY", 1.5f);

                    eggScaleX.setDuration(ts.get(i2));
                    eggScaleY.setDuration(ts.get(i2+1));

                    AnimatorSet eggScaleUp = new AnimatorSet();
                    eggScaleUp.play(eggScaleX).with(eggScaleY);
                    eggScaleUp.start();
                }
            }, ts.get(i2+2));

            final int i3 = i2+3;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    eggImage.setImageDrawable(dragon2);
                }
            }, ts.get(i3));

            final int i4 = i3+1;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator translator = ObjectAnimator.ofFloat(homeImage, "x", 2000f);
                    translator.setDuration(ts.get(i4));
                    translator.start();
                    translator = ObjectAnimator.ofFloat(eggImage, "x", -2000f);
                    translator.setDuration(ts.get(i4+1));
                    translator.start();
                }
            }, ts.get(i4+2));

            final int i5 = i4+3;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator translator = ObjectAnimator.ofFloat(homeImage, "x", -2200f);
                    translator.setDuration(ts.get(i5));
                    translator.start();
                    translator = ObjectAnimator.ofFloat(eggImage, "x", 2800f);
                    translator.setDuration(ts.get(i5+1));
                    translator.start();
                }
            }, ts.get(i5+2));

            final int i6 = i5+3;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator translator = ObjectAnimator.ofFloat(homeImage, "x", 500f);
                    translator.setDuration(ts.get(i6));
                    translator.start();
                    translator = ObjectAnimator.ofFloat(eggImage, "x", 600f);
                    translator.setDuration(ts.get(i6+1));
                    translator.start();
                }
            }, ts.get(i6+2));

            final int i7 = i6+3;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    button.setVisibility(View.VISIBLE);
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(button, "alpha", .3f, 1f);
                    fadeIn.setDuration(ts.get(i7));
                    fadeIn.start();
                }
            }, ts.get(i7+1));

        }

        
        else {
            // In portrait
            final int i1 = i;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    button.setVisibility(View.INVISIBLE);

                    ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(homeImage, "scaleX", 3f);
                    ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(homeImage, "scaleY", 3f);
                    ObjectAnimator rotator = ObjectAnimator.ofFloat(homeImage, "rotation", 0f, 360f);
                    ObjectAnimator translator;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Path path = new Path();
                        path.arcTo(0f, 0f, 1000f, 1000f, 270f, -180f, true);
                        translator = ObjectAnimator.ofFloat(homeImage, View.X, View.Y, path);
                    } else {
                        translator = ObjectAnimator.ofFloat(homeImage, "y", 1000f);
                    }

                    scaleUpX.setDuration(ts.get(i1));
                    scaleUpY.setDuration(ts.get(i1+1));
                    rotator.setDuration(ts.get(i1+2));
                    translator.setDuration(ts.get(i1+3));

                    AnimatorSet scaleUp = new AnimatorSet();
                    scaleUp.play(scaleUpX).with(scaleUpY).with(rotator).with(translator);
                    scaleUp.start();

                    ObjectAnimator eggRotator = ObjectAnimator.ofFloat(eggImage, "rotation", 0f, 360f);

                    eggRotator.setDuration(ts.get(i1+4));
                    eggRotator.start();
                }
            }, ts.get(i1+5));

            final int i2 = i1+6;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator eggScaleX = ObjectAnimator.ofFloat(eggImage, "scaleX", 2f);
                    ObjectAnimator eggScaleY = ObjectAnimator.ofFloat(eggImage, "scaleY", 2f);

                    eggScaleX.setDuration(ts.get(i2));
                    eggScaleY.setDuration(ts.get(i2+1));

                    AnimatorSet eggScaleUp = new AnimatorSet();
                    eggScaleUp.play(eggScaleX).with(eggScaleY);
                    eggScaleUp.start();
                }
            }, ts.get(i2+2));

            final int i3 = i2+3;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    eggImage.setImageDrawable(dragon2);
                }
            }, ts.get(i3));

            final int i4=i3+1;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator translator = ObjectAnimator.ofFloat(homeImage, "x", 2000f);
                    translator.setDuration(ts.get(i4));
                    translator.start();
                    translator = ObjectAnimator.ofFloat(eggImage, "x", -2000f);
                    translator.setDuration(ts.get(i4+1));
                    translator.start();
                }
            }, ts.get(i4+2));

            final int i5 = i4+3;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator translator = ObjectAnimator.ofFloat(homeImage, "x", -1400f);
                    translator.setDuration(ts.get(i5));
                    translator.start();
                    translator = ObjectAnimator.ofFloat(eggImage, "x", 1800f);
                    translator.setDuration(ts.get(i5+1));
                    translator.start();
                }
            }, ts.get(i5+2));

            final int i6 = i5+3;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator translator = ObjectAnimator.ofFloat(homeImage, "x", 400f);
                    translator.setDuration(ts.get(i6));
                    translator.start();
                    translator = ObjectAnimator.ofFloat(eggImage, "x", 300f);
                    translator.setDuration(ts.get(i6+1));
                    translator.start();
                }
            }, ts.get(i6+2));

            final int i7 = i6+3;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    button.setVisibility(View.VISIBLE);
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(button, "alpha", .3f, 1f);
                    fadeIn.setDuration(ts.get(i7));
                    fadeIn.start();
                }
            }, ts.get(i7+1));

        }
    }
}
