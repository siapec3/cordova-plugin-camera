package smsgi.com.br.cameraapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.camera.CustomLayout;

import smsgi.com.br.cameraapp.reflect.Meta;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

/**
 * Created by desenvolvimento10 on 03/07/18.
 */

public class AppCameraSm extends CustomLayout {

    protected final static String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static String TAG = "AppCameraSm";

    public AppCameraSm(CordovaInterface cordovaInterface, View viewGet, CordovaWebView viewWeb, CameraWorker worker, CallbackContext callbackContext) {
        super(cordovaInterface, viewGet, viewWeb, worker, callbackContext);

        try {
            cameraPrevisualizacao();
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "0000000000000000000000000000011111111111111111111122222222222222222222222   onCreate: ????????????????????????????????????????????????????????");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mCamera.stopPreview();
        mCamera = null;
    }


    public void cameraPrevisualizacao() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppCameraSm.getView.setVisibility(View.INVISIBLE);
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                layoutPrincipal = new FrameLayout(activity);
                layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutPrincipal.setLayoutParams(layoutParams);
                capturedImageHolder = new ImageView(activity);
                getResourcesById("list", "id", capturedImageHolder);
                layoutPrincipal.addView(capturedImageHolder);

                onClickFoto(capturedImageHolder);
                // Get reference to display
                Display display = activity.getWindowManager().getDefaultDisplay();
                Context context = AppCameraSm.webView.getContext();

                if (checkCameraHardware(activity)) {
                    mCamera = getCameraInstance();
                    mPreview = null;
                    mPreview = new CameraPreview(activity, mCamera);
                    layoutPrincipal.addView(mPreview);

                    linhaDeComandos();
                    botaoTirarFoto();

                    initialize(context);
                }

            }
        });
    }



    public void onClickFoto(ImageView button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureButton.setVisibility(View.INVISIBLE);
                botaoExcluir();
                mCamera.takePicture(null, null, mPicture);
                linhaDeAcoes.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initialize(Context context) {

        dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                    PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
                    r.setKeepCallback(true);
                    callbackContext.sendPluginResult(r);
                    callbackErrorPluginCordova();
                    getView.setVisibility(View.VISIBLE);
                }
            }
        };

        dialog.setContentView(layoutPrincipal);
        dialog.setCancelable(false);
        dialog.show();
//        layoutPrincipal.setContentView(activity.getResources().getIdentifier("cordova_camera_plugin", "layout", getPackageName()));
//        layoutPrincipal.setContentView(Meta.getResId(activity, "layout", "cordova_camera_plugin"));
    }

    public void linhaDeComandos() {
        linhaDeAcoes = new LinearLayout(activity);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lllp.gravity = Gravity.BOTTOM;
        getLinearResourcesById("custom", "id", linhaDeAcoes);
        linhaDeAcoes.setVisibility(View.INVISIBLE);
        linhaDeAcoes.setBackgroundColor(Color.BLACK);
        linhaDeAcoes.setBaselineAligned(true);
        linhaDeAcoes.setLayoutParams(lllp);

        botaoConfirmacao();
    }


    public void botaoTirarFoto() {
        CustomLayout.captureButton = criarImageButton("obturador2", "button1");
//        CustomLayout.captureButton = (ImageButton) findViewById(android.R.id.button1);
        onClickFoto(CustomLayout.captureButton);
        CustomLayout.captureButton.setBackground(null);
        LinearLayout linearLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lllp.gravity = Gravity.BOTTOM;
        lllp.setMargins(30,0,0,30);
        CustomLayout.captureButton.setLayoutParams(lllp);
        linearLayout.addView(CustomLayout.captureButton);
        layoutPrincipal.addView(linearLayout);
    }

    public void botaoConfirmacao() {

        CustomLayout.confirm = criarImageButton("confirm", "button2");
        CustomLayout.confirm.setOnClickListener(new View.OnClickListener() {
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
                    linhaDeComandos();
                    botaoTirarFoto();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                        dialog = null;
                        getView.setVisibility(View.VISIBLE);
//                        callbackErrorPluginCordova();
                    }
//                        System.exit(1);
                }
                return;
            }
        });

        linhaDeAcoes.addView(CustomLayout.confirm);
        layoutPrincipal.addView(linhaDeAcoes);
    }

    public void botaoExcluir() {
        // construcao do GRID para o layout dos botoes excluir e salvar

        CustomLayout.exclude = criarImageButton("trash", "button3");
        CustomLayout.exclude.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                file.delete();
                try {
                    if (checkCameraHardware(activity)) {
                        if (mCamera != null) {
                            mCamera = null;
                        }
                        mCamera = getCameraInstance();
                        Parameters params = mCamera.getParameters();
                        params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
                        params.setPictureFormat(ImageFormat.JPEG);
                        Camera.Size size = getSmallestPictureSize(params);
                        params.setPictureSize(size.width, size.height);
                        mCamera.setParameters(params);

                        layoutPrincipal.removeAllViews();
                        capturedImageHolder = new ImageView(cordova.getActivity());
                        layoutPrincipal.addView(capturedImageHolder);
                        mPreview = new CameraPreview(activity, mCamera);
                        layoutPrincipal.addView(mPreview);
                        linhaDeComandos();
                        botaoTirarFoto();
//                        callbackErrorPluginCordova();
                        layoutPrincipal.removeView(progress);
                    }
                    linhaDeAcoes.setVisibility(View.INVISIBLE);
                    CustomLayout.captureButton.setVisibility(View.VISIBLE);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "retornar a camera" + e.getMessage().toString());
                    callbackErrorPluginCordova();
                }
            }
        });
        linhaDeAcoes.addView(CustomLayout.exclude);
    }


}