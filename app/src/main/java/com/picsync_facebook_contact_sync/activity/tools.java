package com.picsync_facebook_contact_sync.activity;

import android.app.Application;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by gary on 2015/9/7.
 */
public class tools extends Application {
    public static byte[] bitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
        return baos.toByteArray();
    }
}
