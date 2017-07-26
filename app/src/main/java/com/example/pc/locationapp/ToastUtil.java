package com.example.pc.locationapp;

import android.content.Context;
import android.os.storage.StorageManager;
import android.widget.Toast;

/**
 * Created by pc on 2017-07-14.
 */

public class ToastUtil {
    private  static Toast mToast;

    public  static  void showToast(Context context, String msg){
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        mToast.show();
    }
}
