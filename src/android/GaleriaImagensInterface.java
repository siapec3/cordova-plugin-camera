package smsgi.com.br.cameraapp;

import android.view.View;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

/**
 * Created by desenvolvimento10 on 03/07/18.
 */

public class GaleriaImagensInterface {


    public GaleriaImagensInterface(CordovaInterface cordovaInterface, View viewGet, CordovaWebView viewWeb, GaleriaWorker worker, CallbackContext callbackContext) {

        try {
//            cameraPrevisualizacao();
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }
}
