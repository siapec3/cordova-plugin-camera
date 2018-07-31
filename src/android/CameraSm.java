package org.apache.cordova.camera;

import android.Manifest;
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
import android.hardware.Camera.CameraInfo;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by desenvolvimento10 on 03/07/18.
 */

public class CameraSm extends Activity {

    protected final static String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static View getView;
    public static CordovaWebView webView;
    public static CordovaInterface cordova;
    public static ImageButton captureButton;
    public static ImageButton confirm;
    public static ImageButton exclude;
    private static String TAG = "CameraSm";
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout layoutPrincipal;
    private ImageView capturedImageHolder;
    private LinearLayout btnConfirmarExcluir;
    private File file;
    private Activity activity;
    //    private RelativeLayout.LayoutParams relativeLayoutParams;
    private Dialog dialog;

    public CameraSm() {
    }

    // Helper to be compile-time compatible with both Cordova 3.x and 4.x.
    public static Camera getCameraInstance() {
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


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
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

                ExifInterface exif=new ExifInterface(file.toString());

                Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
                    realImage= rotate(realImage, 90);
                } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
                    realImage= rotate(realImage, 270);
                } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
                    realImage= rotate(realImage, 180);
                } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
                    realImage= rotate(realImage, 90);
                }

                boolean bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "Arquivo nao encontrado " + e.getMessage());
            } catch (IOException ex) {
                Log.d(TAG, "Error acessando o arquivo: " + ex.getMessage());
            }
        }
    };


    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    /**
     * @param cordovaInterface
     * @param viewGet
     * @param viewWeb
     */
    public void cameraPrevisualizacao(CordovaInterface cordovaInterface, View viewGet, CordovaWebView viewWeb) {
        activity = cordovaInterface.getActivity();
        CameraSm.cordova = cordovaInterface;
        CameraSm.getView = viewGet;
        CameraSm.webView = viewWeb;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getView.setVisibility(View.INVISIBLE);
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Get reference to display
                Display display = activity.getWindowManager().getDefaultDisplay();
                Context context = webView.getContext();


                layoutPrincipal = new FrameLayout(activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutPrincipal.setLayoutParams(layoutParams);
//                capturedImageHolder = (ImageView) getResourcesById("list", "id", capturedImageHolder);
                capturedImageHolder = new ImageView(activity);
                getResourcesById("list", "id", capturedImageHolder);
//                relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//                relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
//                capturedImageHolder.setLayoutParams(relativeLayoutParams);
                if (checkCameraHardware(activity)) {
                    mCamera = getCameraInstance();
                    mPreview = null;
                    mPreview = new CameraPreview(activity, mCamera);
                    layoutPrincipal.addView(mPreview);
                    botaoTirarFoto();
                    botoesConfirmarExcluir();
                }
//                getFilesResources("captured_image", "id", capturedImageHolder);
                layoutPrincipal.addView(capturedImageHolder);

                dialog = new Dialog(context) {
                    @Override
                    public void onBackPressed() {
                        super.onBackPressed();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                            dialog = null;
                            getView.setVisibility(View.VISIBLE);
                        }
                    }
                };

                dialog.setContentView(layoutPrincipal);
                dialog.setCancelable(false);
                dialog.show();
            }
        });

//        setContentView(Meta.getResId(activity, "layout", "app_camera_activity"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
//        mCamera.stopPreview();
        mCamera = null;
    }

    private void botaoTirarFoto() {
//        captureButton = (ImageButton) getView.findViewById(android.R.id.icon_frame);
//        captureButton = (ImageButton) getFilesResources("button1", "id", captureButton);
        CameraSm.captureButton = new ImageButton(activity);
        CameraSm.captureButton = (ImageButton) getResourcesById("button1", "id", captureButton);
//        captureButton.setBackground(null);
//        captureButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        captureButton.setImageResource(android.R.drawable.alert_dark_frame);

//        relativeLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
//        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        relativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
//        captureButton.setLayoutParams(relativeLayoutParams);

        CameraSm.captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                captureButton.setVisibility(View.INVISIBLE);
                mCamera.takePicture(null, null, mPicture);

                btnConfirmarExcluir.setVisibility(View.VISIBLE);
            }
        });
        getFilesResources("camera", "drawable", CameraSm.captureButton);
        layoutPrincipal.addView(CameraSm.captureButton);
    }

    private void botoesConfirmarExcluir() {
//        btnConfirmarExcluir = (LinearLayout) getView.findViewById(android.R.id.custom);
        btnConfirmarExcluir = new LinearLayout(activity);
        getLinearResourcesById("custom", "id", btnConfirmarExcluir);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnConfirmarExcluir.setVisibility(View.INVISIBLE);
//        btnConfirmarExcluir.setBackground(null);
//        btnConfirmarExcluir.setBaselineAligned(true);
//        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        relativeLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
//        btnConfirmarExcluir.setLayoutParams(lp);

//        final ImageButton confirm = (ImageButton) getView.findViewById(android.R.id.button2);
        CameraSm.confirm = new ImageButton(activity);
        getResourcesById("button2", "id", CameraSm.confirm);
//        confirm.setImageResource(android.R.drawable.confirm);
        CameraSm.confirm.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        confirm.setBackground(null);
//        relativeLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
//        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        relativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
//        confirm.setLayoutParams(relativeLayoutParams);
        getFilesResources("confirm", "drawable", CameraSm.confirm);
        CameraSm.confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                file.delete();

                if (checkCameraHardware(activity)) {
                    if (mCamera != null) {
                        mCamera = null;
                    }
                    mCamera = getCameraInstance();
                    layoutPrincipal.removeAllViews();
                    capturedImageHolder = new ImageView(cordova.getActivity());
                    layoutPrincipal.addView(capturedImageHolder);
                    mPreview = new CameraPreview(activity, mCamera);
                    layoutPrincipal.addView(mPreview);
                    botaoTirarFoto();
                    botoesConfirmarExcluir();
//                        if (dialog != null && dialog.isShowing()) {
//                            dialog.dismiss();
//                            dialog = null;
//                            getView.setVisibility(View.VISIBLE);
//                        }
//                        System.exit(1);
                }
                return;
            }
        });
        layoutPrincipal.addView(btnConfirmarExcluir);
        btnConfirmarExcluir.addView(CameraSm.confirm);

        //botão excluir
//        final ImageButton exclude = (ImageButton) getView.findViewById(android.R.id.button3);
        CameraSm.exclude = new ImageButton(activity);
        getResourcesById("button3", "id", confirm);
//        exclude.setBackground(null);
//        exclude.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        exclude.setImageResource(android.R.drawable.trash);
//        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        relativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
//        exclude.setLayoutParams(relativeLayoutParams);
        getFilesResources("trash", "drawable", exclude);
        CameraSm.exclude.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                file.delete();
                try {
                    if (checkCameraHardware(activity)) {
                        if (mCamera != null) {
                            mCamera = null;
                        }
                        mCamera = getCameraInstance();
                        layoutPrincipal.removeAllViews();
                        capturedImageHolder = new ImageView(cordova.getActivity());
                        layoutPrincipal.addView(capturedImageHolder);
                        mPreview = new CameraPreview(activity, mCamera);
                        layoutPrincipal.addView(mPreview);
                        botaoTirarFoto();
                        botoesConfirmarExcluir();
//                        if (dialog != null && dialog.isShowing()) {
//                            dialog.dismiss();
//                            dialog = null;
//                            getView.setVisibility(View.VISIBLE);
//                        }
                        throw new IllegalArgumentException("Imagem não foi capturada");
//                        System.exit(1);
                    }
                    btnConfirmarExcluir.setVisibility(View.INVISIBLE);
                    CameraSm.captureButton.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.d(TAG, "retornar a camera" + e.getMessage().toString());
                }
            }
        });
        btnConfirmarExcluir.addView(CameraSm.exclude);
    }

    /**
     * Recupera os arquivos estaticos como Imagens, XML, Activities
     *
     * @param nome      Id do componente que espera recuperar
     * @param tipo      drawlable ou/ id
     * @param component ImageButton, ImageView, ContentType
     * @return
     */
    private ImageView getFilesResources(String nome, String tipo, ImageView component) {
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

    private ImageView getResourcesById(String nome, String tipo, ImageView component) {
        Resources activityRes = activity.getResources();
        int resId = activityRes.getIdentifier(nome, tipo, activity.getPackageName());
        if (Build.VERSION.SDK_INT >= 16) {
            component.setBackground(null);
        } else {
            component.setBackgroundDrawable(null);
        }

        return component;
    }

    private void getLinearResourcesById(String nome, String tipo, LinearLayout component) {
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
                CameraInfo info = new CameraInfo();
                if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                    return i;
                }
            }
        }
        return 0;
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    private void getOutputMediaFile(int type) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "IMG_" + timeStamp + ".jpg");
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}