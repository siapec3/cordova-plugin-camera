package org.apache.cordova.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import smsgi.com.br.cameraapp.AppCameraSm;
import smsgi.com.br.cameraapp.CameraPreview;
import smsgi.com.br.cameraapp.CameraWorker;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by desenvolvimento10 on 03/07/18.
 */

public class CustomLayout extends AppCompatActivity implements CameraWorker.CameraCallBack {

    public static ImageButton captureButton;
    public static ImageButton confirm;
    public static ImageButton exclude;
    public static View getView;
    public static CordovaWebView webView;
    public static CordovaInterface cordova;
    private static String TAG = "CustomLayout";
    protected Camera mCamera;
    protected CameraPreview mPreview;
    protected File file;
    protected Activity activity;
    protected LinearLayout linhaDeAcoes;
    protected FrameLayout layoutPrincipal;
    protected ImageView capturedImageHolder;
    protected FrameLayout.LayoutParams layoutParams;
    protected Dialog dialog;
    protected ProgressBar progress;
    protected CameraWorker myCallbackClass;
    protected CallbackContext callbackContext;
    private CameraWorker worker;
    /**
     * @param cordovaInterface
     * @param viewGet
     * @param viewWeb
     */
    public CustomLayout(CordovaInterface cordovaInterface, View viewGet, CordovaWebView viewWeb, CameraWorker worker, CallbackContext callback) {
        activity = cordovaInterface.getActivity();
        this.callbackContext = callback;
        this.worker = worker;
        AppCameraSm.cordova = cordovaInterface;
        AppCameraSm.getView = viewGet;
        AppCameraSm.webView = viewWeb;
        worker.registerCallback(this);
    }

    protected void callbackErrorPluginCordova(){
        callbackContext.error("Illegal Argument Exception" + PluginResult.Status.ERROR);
        PluginResult r = new PluginResult(PluginResult.Status.ERROR);
        callbackContext.sendPluginResult(r);
        return;
    }

    // Helper to be compile-time compatible with both Cordova 3.x and 4.x.
    protected static Camera getCameraInstance() {

        Camera c = null;
        try {
            c = Camera.open();
            Camera.Parameters params = c.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setPictureSize(1024, 768);
            c.setParameters(params);
            Log.d(TAG, "camera ok ");
        } catch (Exception e) {
            Log.e(TAG, "camera nao disponivel " + e);
        }
        return c;
    }

    protected ImageButton criarImageButton(String nomeIcon, String nomeComponent) {
        ImageButton imageButton = new ImageButton(activity);
        imageButton = (ImageButton) getResourcesById(nomeComponent, "id", imageButton);
//        imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageButton.setBackground(null);
        //ação do botao na camada anterior
        getFilesResources(nomeIcon, "drawable", imageButton);
        return imageButton;
    }

    /**
     * Recupera os arquivos estaticos como Imagens, XML, Activities
     *
     * @param nome      Id do componente que espera recuperar
     * @param tipo      drawlable ou/ id
     * @param component ImageButton, ImageView, ContentType
     * @return
     */
    protected ImageView getFilesResources(String nome, String tipo, ImageView component) {
        Resources activityRes = activity.getResources();
        int resId = activityRes.getIdentifier(nome, tipo, activity.getPackageName());
        Drawable icon = activityRes.getDrawable(resId);
        if (Build.VERSION.SDK_INT >= 16) {
            component.setBackground(null);
        } else {
            component.setBackgroundDrawable(null);
        }
        component.setImageDrawable(icon);

        return component;
    }

    protected ImageView getResourcesById(String nome, String tipo, ImageView component) {
        Resources activityRes = activity.getResources();
        int resId = activityRes.getIdentifier(nome, tipo, activity.getPackageName());

        if (Build.VERSION.SDK_INT >= 16) {
            component.setBackground(null);
        } else {
            component.setBackgroundDrawable(null);
        }

        return component;
    }

    protected void getLinearResourcesById(String nome, String tipo, LinearLayout component) {
        Resources activityRes = activity.getResources();
        int resId = activityRes.getIdentifier(nome, tipo, activity.getPackageName());
        if (Build.VERSION.SDK_INT >= 16) {
            component.setBackground(null);
        } else {
            component.setBackgroundDrawable(null);
        }
    }

    private int findBackCamera() {
        int totalDeCameras = Camera.getNumberOfCameras();
        if (totalDeCameras > 1) {
            for (int i = 0; i < totalDeCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    return i;
                }
            }
        }
        return 0;
    }

    protected boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, ">>>>>>>>>>>>>>>>   Chamou a onRequestPermissionsResult ?????????????????????????????????????????????????????");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200:
                boolean camera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean file = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(file);
    }



    protected Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (file == null) {
                Log.d(TAG, "Error para criar o arquivo, check as permissoes de gravação: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                ExifInterface exif = new ExifInterface(file.toString());

                Log.d("Exif value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                    realImage = rotate(realImage, 90);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                    realImage = rotate(realImage, 270);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                    realImage = rotate(realImage, 180);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")) {
                    realImage = rotate(realImage, 90);
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                boolean bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapdata = bos.toByteArray();

                fos.write(bitmapdata);
                fos.close();
                worker.mCallBack.onSuccess(getFile());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "Arquivo nao encontrado " + e.getMessage());
            } catch (IOException ex) {
                Log.d(TAG, "Error acessando o arquivo: " + ex.getMessage());
            }
        }
    };

    private static Bitmap rotate(Bitmap bitmap, int degree){
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx,true);
    }

    protected Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        Collections.sort(list, new AreasComparator());

        return list.get(1); // I choose the second one becasue the first one is too small.
    }

    public class AreasComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size s1, Camera.Size s2) {

            int resultArea=s1.width * s1.height;
            int newArea=s2.width * s2.height;

            if (newArea < resultArea) {
                return 1;
            }

            return -1;
        }
    }


    @Override
    public void getOutputMediaFile(Integer type) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File cache = null;
        // SD Card Mounted
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cache = cordova.getActivity().getExternalCacheDir();
        }
        // Use internal storage
        else {
            cache = cordova.getActivity().getCacheDir();
        }
        // Create the cache directory if it doesn't exist
        cache.mkdirs();
        cache.getAbsolutePath();
        // setFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"IMG_"+timeStamp+".jpg"));
       setFile(new File(cache.getAbsolutePath(), "IMG_" + timeStamp + ".jpg")); //Ira funcionar dessa forma mas para testar o formato da imagem preciso ver como fica

    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

}
