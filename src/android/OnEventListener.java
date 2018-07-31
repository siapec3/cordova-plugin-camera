package org.apache.cordova.camera;

/**
 * Created by desenvolvimento10 on 04/07/18.
 */

public interface OnEventListener<T> {
    public void onSuccess(T object);
    public void onFailure(Exception e);
}
