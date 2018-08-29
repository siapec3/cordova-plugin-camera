package smsgi.com.br.cameraapp;

import android.hardware.Camera;
import android.os.Bundle;

/**
 * Created by desenvolvimento10 on 29/08/18.
 */

public interface OnFocusListener {

    void onCreate(Bundle savedInstanceState);

    void onResume();

    Camera getCameraInstance(int currentCameraId);

    void onFocused();

}
