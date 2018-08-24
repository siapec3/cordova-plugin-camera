package smsgi.com.br.cameraapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by desenvolvimento10 on 03/07/18.
 */

public abstract class CustomLayout extends AppCompatActivity implements CameraWorker.CameraCallBack {

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
    protected Dialog preVisualizacaoDialog;
    protected ProgressBar progress;
    protected CallbackContext callbackContext;
    protected CameraWorker worker;
    private FrameLayout previewLayout;
    private ProgressBar andamentoProcesso;
    private BitmapFactory.Options options;
    private Bitmap bitmap;
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
        options = new BitmapFactory.Options();
        options.inMutable = true;

    }

    protected void mostrarObturador(){
        linhaDeAcoes.setVisibility(View.INVISIBLE);
        CustomLayout.captureButton.setVisibility(View.VISIBLE);
    }

    protected void mostrarBarraDeFerramentas(){
        linhaDeAcoes.setVisibility(View.VISIBLE);
        CustomLayout.captureButton.setVisibility(View.INVISIBLE);
    }

    protected void callbackErrorPluginCordova(){
        callbackContext.error("Illegal Argument Exception" + PluginResult.Status.ERROR);
        PluginResult r = new PluginResult(PluginResult.Status.ERROR);
        callbackContext.sendPluginResult(r);
        if (file.isFile())  file.delete(); else  Log.d(TAG, ">>>>>>>>>>>>>>>>  nenhum arquivo encontrado ");
        worker.mCallBack.onFailure(new Exception("Imagem não selecionada"));
        return;
    }

    // Helper to be compile-time compatible with both Cordova 3.x and 4.x.
    protected static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
            Camera.Parameters params = c.getParameters();
            List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setPictureFormat(ImageFormat.JPEG);
//            params.setPictureSize(640, 480);
            // You need to choose the most appropriate previewSize for your app
            int w = 0; int h = 0;
            for (int i=0; i < previewSizes.size(); i++) {
                Log.d(TAG, ">>>>>>>>>>>>>>>>  getCameraInstance Width x Heigth " + previewSizes.get(i).width +" x "+ previewSizes.get(i).height);
                w = ( w < previewSizes.get(i).width && (previewSizes.get(i).width < 964))?  previewSizes.get(i).width : w ;
                h = ( h < previewSizes.get(i).height && (previewSizes.get(i).height < 750))? previewSizes.get(i).height : h ;
            }
            Log.d(TAG, ">>>>>>>>>>>>>>>>  getCameraInstance " + w +"x"+ h);
            params.setPictureSize(w, h);
            params.setPreviewSize(w, h);
            c.setParameters(params);
            Log.d(TAG, "camera ok getCameraInstance: " + c);
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
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(options, 640, 640);
//                fos.write(data);

//                Bitmap realImage = BitmapFactory.decodeFile(file.getAbsolutePath(), options);


                ExifInterface exif = new ExifInterface(file.toString());

                Log.d("Exif value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                    realImage = rotate(realImage, 90);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                    realImage = rotate(realImage, 270);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                    realImage = rotate(realImage, 180);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0") || exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("1")) {
                    realImage = rotate(realImage, 90);
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                boolean bo = realImage.compress(Bitmap.CompressFormat.JPEG, 50, bos);
                byte[] bitmapdata = bos.toByteArray();

                fos.write(bitmapdata);
                fos.close();
                preVisualizacao(activity);
                layoutPrincipal.removeAllViews();
//                worker.mCallBack.onSuccess(getFile());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "Arquivo nao encontrado " + e.getMessage());
                worker.mCallBack.onFailure(e);
            } catch (IOException ex) {
                Log.d(TAG, "Error acessando o arquivo: " + ex.getMessage());
                worker.mCallBack.onFailure(ex);
            }
        }
    };


    private Bitmap rotate(Bitmap bitmap, int degree){


        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx,true);
    }



    protected void preVisualizacao(Context context) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                previewLayout = new FrameLayout(activity);
                andamentoProcesso = processando();
                previewLayout.addView(andamentoProcesso);
                preVisualizacaoDialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                    @Override
                    public void onBackPressed() {
                        super.onBackPressed();
//                        dialogDeComandos();
                        finish();

                    }
                };

                preVisualizacaoDialog.setContentView(previewLayout);
                preVisualizacaoDialog.setCancelable(false);
                preVisualizacaoDialog.show();
                ImageView imagemPreview = new ImageView(activity);
                try {
                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 3;
                    bitmap = BitmapFactory.decodeFile(getFile().getAbsolutePath(), options);

                    options.inJustDecodeBounds = false;
                    options.inSampleSize = calculateInSampleSize(options, 500, 500);
                    bitmap = BitmapFactory.decodeFile(getFile().getAbsolutePath(), options);


                    imagemPreview.setImageBitmap(bitmap);
                }catch(Exception ex){
                    LOG.d(TAG , ex.getMessage());
                }
//                imagemPreview.setImageURI(Uri.fromFile(getFile()));
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                previewLayout.setLayoutParams(layoutParams);
                previewLayout.addView(imagemPreview);
                enviarFoto();
                previewLayout.removeView(andamentoProcesso);
//                barraDeComandosTopo();

            }
        });
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    private void enviarFoto() {
        ImageButton enviarFotoButton = criarImageButton("paperfly_send", "button_foto");

        enviarFotoButton.setBackground(null);
        LinearLayout linearLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lllp.gravity = Gravity.BOTTOM;
        lllp.setMargins(0,0,20,60);
        enviarFotoButton.setLayoutParams(lllp);
        linearLayout.addView(enviarFotoButton);
        enviarFotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
//                dialogDeComandos();
                finish();
            }
        });
        previewLayout.addView(linearLayout);
    }

    protected abstract void barraDeComandosTopo();

    protected abstract void dialogDeComandos();

    private ProgressBar processando() {
        ProgressBar progress = new ProgressBar(activity);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progress.setLayoutParams(lp);
        return progress;
    }

    protected Camera.Size getLargestPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        Collections.sort(list, new AreasComparator());

        return list.get(1); // I choose the second one becasue the first one is too small.
    }

    public class AreasComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size s1, Camera.Size s2) {

            int resultArea=s1.width * s1.height;
            int newArea=s2.width * s2.height;

            if (newArea > resultArea) {
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
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

}
