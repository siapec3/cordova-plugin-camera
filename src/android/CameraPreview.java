package smsgi.com.br.cameraapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by desenvolvimento10 on 28/06/18.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Activity activity;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.activity = (Activity) context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "criado");
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error setting camera preview: "+e.getMessage());
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "changed");
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.d(TAG, "nao parou o preview");
        }
        //tamanho do preview, resize, rotacao ou reformatacao da imagem
//        int orientacao = this.getResources().getConfiguration().orientation;
//        switch (orientacao) {
//            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
//                mCamera.setDisplayOrientation(0);
//                break;
//            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
//                mCamera.setDisplayOrientation(90);
//                break;
//            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
//                mCamera.setDisplayOrientation(270);
//                break;
//            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
//                mCamera.setDisplayOrientation(180);
//                break;
//            default:
//                mCamera.setDisplayOrientation(0);
//                break;
//        }
        this.getScreenOrientation();
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error iniciando a camera preview: "+e.getMessage());
        }
    }

    private int getScreenOrientation() {
        int rotation = this.activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        this.activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    mCamera.setDisplayOrientation(90);
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    mCamera.setDisplayOrientation(0);
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    mCamera.setDisplayOrientation(270);
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    mCamera.setDisplayOrientation(180);
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    mCamera.setDisplayOrientation(0);
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    mCamera.setDisplayOrientation(90);
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    mCamera.setDisplayOrientation(180);
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    mCamera.setDisplayOrientation(270);
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "destruindo");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }
}
