package com.example.administrator.facepptest;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2017/11/8.
 */

public class BitmaptoArray {
    private Bitmap bmSmall;
    public BitmaptoArray(Bitmap bitmap){
        this.bmSmall = bitmap;
        Bitmap bm = Bitmap.createBitmap(bmSmall,0,0, bmSmall.getWidth(),bmSmall.getHeight());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] arrys = stream.toByteArray();

    }
}
