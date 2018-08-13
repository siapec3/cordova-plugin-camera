package smsgi.com.br.galeriasmview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.View;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import java.io.File;

/**
 * Created by desenvolvimento10 on 03/07/18.
 */

public abstract class GaleriaImagensInterface extends CordovaActivity implements GaleriaWorker.GaleriaCallback {

    protected Activity activity;
    public static CordovaWebView webView;
    public static CordovaInterface cordova;
    protected CallbackContext callbackContext;
    protected GaleriaWorker worker;
    public static View getView;
    private File file;
    protected Dialog dialog;

    public GaleriaImagensInterface(CordovaInterface cordovaInterface, View viewGet, CordovaWebView viewWeb, GaleriaWorker worker, CallbackContext callbackContext) {
        activity = cordovaInterface.getActivity();
        this.callbackContext = callbackContext;
        this.worker = worker;
        GaleriaImagensInterface.cordova = cordovaInterface;
        GaleriaImagensInterface.getView = viewGet;
        GaleriaImagensInterface.webView = viewWeb;
        worker.registerCallback(this);
        try {
            galeriaPrevisualizacao();
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public void galeriaPrevisualizacao() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GaleriaImagensInterface.getView.setVisibility(View.INVISIBLE);
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Get reference to display
                Display display = activity.getWindowManager().getDefaultDisplay();
                Context context = GaleriaImagensInterface.webView.getContext();

                initialize(context);


            }
        });
    }

    protected abstract void initialize(Context context);



    protected void callbackErrorPluginCordova(){
        callbackContext.error("Illegal Argument Exception" + PluginResult.Status.ERROR);
        PluginResult r = new PluginResult(PluginResult.Status.ERROR);
        callbackContext.sendPluginResult(r);
        if (file != null){
            if (file.isFile())  file.delete(); else  Log.d(TAG, ">>>>>>>>>>>>>>>>  nenhum arquivo encontrado ");
        }
        worker.mCallBack.onFailure(new Exception("Imagem não selecionada"));
        return;
    }

    @Override
    public void getOutputMediaFile(Integer type) {

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
