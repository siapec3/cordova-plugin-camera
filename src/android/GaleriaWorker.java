package smsgi.com.br.galeriasmview;

import android.os.AsyncTask;
import android.view.View;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import java.io.File;

import smsgi.com.br.cameraapp.OnEventListener;

/**
 * Created by desenvolvimento10 on 04/07/18.
 */
public class GaleriaWorker extends AsyncTask<Void, Void, File> {

    File file;

    public OnEventListener<File> mCallBack;
    public Exception mException;
    private int encodingType;

    public GaleriaWorker(){

    }

    /**
     *
     * @param cordovaInterface
     * @param viewGet
     * @param viewWeb
     * @param eventListener
     * @param type
     * @param callbackContext
     */
    public GaleriaWorker(CordovaInterface cordovaInterface, View viewGet, CordovaWebView viewWeb, OnEventListener eventListener, int type, CallbackContext callbackContext) {
        mCallBack = eventListener;
        encodingType = type;
        new GaleriaSmView(cordovaInterface, viewGet, viewWeb, this, callbackContext);
    }

    public interface GaleriaCallback {
        void getOutputMediaFile(Integer type);
        File getFile();
        void setFile(File file);
    }

    GaleriaCallback myCallbackClass;

    public void registerCallback(GaleriaCallback callbackClass){
        myCallbackClass = callbackClass;
    }

    @Override
    protected File doInBackground(Void... voids) {
        try {
            // todo try to do something dangerous
            return getFile();

        } catch (Exception e) {
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File filename) {

        if (mCallBack != null) {
            if (mException == null) {
                myCallbackClass.getOutputMediaFile(encodingType);
            } else {
                mCallBack.onFailure(mException);
            }
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}


