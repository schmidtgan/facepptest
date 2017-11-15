package com.example.administrator.facepptest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.donkingliang.imageselector.utils.ImageSelectorUtils;
import com.google.gson.Gson;
import com.megvii.cloud.http.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 0x00000011;
    private ImageView img1,img2;
    private View mProgressBar;
    private TextView mTip;
    private String ImagePath1,ImagePath2;
    private Bitmap mPhotoimg;
    private StringBuffer mStringBuffer;
    private Handler mUIHandler ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStringBuffer = new StringBuffer();
        mUIHandler = new MyHandler();
        initView();
    }
    private void initView(){
        mTip = (TextView) findViewById(R.id.id_tip);
        mProgressBar= findViewById(R.id.id_waiting);
        findViewById(R.id.id_getImage).setOnClickListener(this);
        findViewById(R.id.id_detect).setOnClickListener(this);
        img1 = (ImageView)findViewById(R.id.img1);
        img2= (ImageView)findViewById(R.id.img2);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.id_getImage:
                ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, false, 2);
                break;
            case R.id.id_detect:
                mProgressBar.setVisibility(View.VISIBLE);
                compareResult(ImagePath1,ImagePath2);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null){
            ArrayList<String> images = data.getStringArrayListExtra(ImageSelectorUtils.SELECT_RESULT);
            ImagePath1=images.get(0);
            ImagePath2=images.get(1);
            Bitmap bit1 = getresizePhoto(ImagePath1);
            Bitmap bit2 = getresizePhoto(ImagePath2);
            img1.setImageBitmap(bit1);
            img2.setImageBitmap(bit2);
        }
    }
    private void compareResult(String imagePath1,String imagePath2){
        if (imagePath1!=null&&imagePath2!=null){
                Faceppcompare.compare(imagePath1,imagePath2, new Faceppcompare.Callback() {
                    @Override
                    public void success(String result) {
                        String confidence = parseJSONWithGson(result);
                        Message msg = Message.obtain();
                        msg.what = Constant.MSG_GOT_DATA;
                        msg.obj = confidence;
                        mUIHandler.sendMessage(msg);
                    }

                    @Override
                    public void error(Exception e) {
                        Message msg = Message.obtain();
                        msg.what = Constant.MSG_EORROR;
                        mUIHandler.sendMessage(msg);
                    }
                });
        }else {
            Toast.makeText(this,"没有图片",Toast.LENGTH_SHORT).show();
        }
    }
    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constant.MSG_GOT_DATA:
                    mProgressBar.setVisibility(View.GONE);
                    String result = (String) msg.obj;
                    mTip.setText("相似度: "+result+"%");
                    break;
                case Constant.MSG_EORROR:
                    mProgressBar.setVisibility(View.GONE);
                    mTip.setText("对比错误,请重试");
                    break;
            }
        }
    }
    private static Bitmap getresizePhoto(String ImagePath){
        if (ImagePath!=null){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(ImagePath,options);
            double ratio = Math.max(options.outWidth*1.0d/1024f,options.outHeight*1.0d/1024);
            options.inSampleSize = (int) Math.ceil(ratio);
            options.inJustDecodeBounds= false;
            Bitmap bitmap=BitmapFactory.decodeFile(ImagePath,options);
            return bitmap;
        }
        return null;
    }
    private String parseJSONWithGson(String JsonData){
        Gson gson = new Gson();
        Face face = gson.fromJson(JsonData,Face.class);
        String confidence = face.getConfidence();
        Log.e("result",confidence);
        return confidence;
    }
}
