package smsgi.com.br.cameraapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;

/**
 * Created by desenvolvimento10 on 28/06/18.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;


    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "criado");
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
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
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(90);
            AppCameraSm.captureButton.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            AppCameraSm.exclude.setLayoutDirection(0);
            AppCameraSm.confirm.setLayoutDirection(0);
        } else {
            mCamera.setDisplayOrientation(0);
            AppCameraSm.captureButton.setLayoutDirection(90);
            AppCameraSm.exclude.setLayoutDirection(90);
            AppCameraSm.confirm.setLayoutDirection(90);
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: "+e.getMessage());
        }
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
