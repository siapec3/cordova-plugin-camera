package org.apache.cordova.camera;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
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

import android.hardware.Camera.Parameters;

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
//        activity.setContentView(activity.getResources().getIdentifier("app_camera_activity", "layout", getPackageName()));
//        activity.setContentView(Meta.getResId(activity, "layout", "app_camera_activity"));
    }

    public void linhaDeComandos() {
        botoesDeAcao = new LinearLayout(activity);
        getLinearResourcesById("custom", "id", botoesDeAcao);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        botoesDeAcao.setVisibility(View.INVISIBLE);
        botoesDeAcao.setBackground(null);
        botoesDeAcao.setBaselineAligned(true);
        botoesDeAcao.setLayoutParams(lp);

        botaoConfirmacao();
        botaoExcluir();
    }

    public void onClickFoto(ImageView button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureButton.setVisibility(View.INVISIBLE);
                mCamera.takePicture(null, null, mPicture);
                botoesDeAcao.setVisibility(View.VISIBLE);
            }
        });
    }

    public void botaoTirarFoto() {
        CustomLayout.captureButton = criarImageButton("camera", "button1");
        onClickFoto(CustomLayout.captureButton);
        layoutPrincipal.addView(CustomLayout.captureButton);
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
                        callbackErrorPluginCordova();
                    }
//                        System.exit(1);
                }
                return;
            }
        });
        layoutPrincipal.addView(botoesDeAcao);
        botoesDeAcao.addView(CustomLayout.confirm);
    }

    public void botaoExcluir() {
        CustomLayout.exclude = criarImageButton("trash", "button3");
        progress = new ProgressBar(webView.getContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progress.setLayoutParams(lp);
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
                        layoutPrincipal.removeView(progress);
                    }
                    botoesDeAcao.setVisibility(View.INVISIBLE);
                    CustomLayout.captureButton.setVisibility(View.VISIBLE);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "retornar a camera" + e.getMessage().toString());
                    callbackErrorPluginCordova();
                }
            }
        });
        botoesDeAcao.addView(CustomLayout.exclude);
    }


}