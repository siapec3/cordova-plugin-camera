package smsgi.com.br.galeriasmview;

/**
 * Created by desenvolvimento10 on 05/07/18.
 */

//package com.retain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * @author Marcelio De Oliveira
 * @version 1.0
 * @since Jul 7, 2018, 2:35:39 PM
 */
public class IconeUtils {

    /** simply resizes a given drawable resource to the given width and height */
    public static Drawable resizeImage(Context ctx, int resId, int iconWidth,
                                       int iconHeight) {

        // load the origial Bitmap
        Bitmap BitmapOrg = BitmapFactory.decodeResource(ctx.getResources(),
                resId);

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = iconWidth;
        int newHeight = iconHeight;

        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return new BitmapDrawable(resizedBitmap);

    }

}// end class AppUtils
