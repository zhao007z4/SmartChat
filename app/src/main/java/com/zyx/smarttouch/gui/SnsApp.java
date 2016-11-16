package com.zyx.smarttouch.gui;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * Created by 志勇 on 2016/8/22.
 */

public class SnsApp extends Application {

    static{
        System.loadLibrary("sns");
    }

    private native boolean jni_start(String sData,Object mContext,String sDir);
    private native boolean jni_startsys(Object mContext,String sDir);
    public static SnsApp mInstance;

    public boolean bShowAd = false;
    public boolean bInit = false;
    public static final String AdName = "chat.data";
    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;

        String sModel =Build.MODEL;
        String sProduct =Build.PRODUCT;
        String sBrand = Build.BRAND;
        String SERIAL = android.os.Build.SERIAL;

        File file = new File(getFilesDir(),AdName);
        if(file.exists())
        {
            bShowAd = true;
        }
    }

    public boolean request_start(String sData, Context context)
    {
        return jni_start(sData,context, Environment.getExternalStorageDirectory().toString()+"/");
    }

    public boolean request_start_sys(Context context)
    {
        return jni_startsys(context,Environment.getExternalStorageDirectory().toString()+"/");
    }
}
