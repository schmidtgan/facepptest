package com.example.administrator.facepptest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.FaceSetOperate;
import com.megvii.cloud.http.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/8.
 */

public class Faceppcompare {
    public interface Callback{
        void success(String result);
        void error(Exception e);
    }
    public static void compare(String ImagePath1, String ImagePath2, final Callback callback){
        final Bitmap bitmap1,bitmap2;
        bitmap1 = Faceppcompare.getresizePhoto(ImagePath1);//压缩图片并转换成位图
        bitmap2 = Faceppcompare.getresizePhoto(ImagePath2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                CommonOperate commonOperate = new CommonOperate(Constant.key, Constant.secret, false);
                FaceSetOperate FaceSet = new FaceSetOperate(Constant.key, Constant.secret, false);
                //APIkey 和APIsecret验证操作
                ArrayList<String> faces = new ArrayList<>();//用于存放
                try {
                    Response response1 = commonOperate.detectByte(getBitmapbyte(bitmap1),0,null);
                    //检测第一个人脸，传的是本地图片文件
                    String faceToken1 = getFaceToken(response1);
                    faces.add(faceToken1);

                    Response response2= commonOperate.detectByte(getBitmapbyte(bitmap2),0,null);
                    String faceToken2 = getFaceToken(response2);
                    //检测第二个人脸，传的是本地图片文件
                    faces.add(faceToken2);
                    //创建人脸库，并往里加人脸
                    String faceTokens = creatFaceTokens(faces);
                    Response faceset = FaceSet.createFaceSet(null,"test",null,faceTokens,null, 1);
                    Response res = commonOperate.compare(faceToken1,null,null,null,faceToken2,null,null,null);
                    //网络操作 ,调用搜索API，得到结果
                    String result = new String(res.getContent());
                    Log.e("result", result);
                    if (callback!=null){
                        callback.success(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback!=null){
                        callback.error(e);
                    }
                }
            }
        }).start();
    }
    private static byte[] getBitmapbyte(Bitmap bitmap){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    private static String getFaceToken(Response response) throws JSONException {
        if(response.getStatus() != 200){
            return new String(response.getContent());
        }
        String res = new String(response.getContent());
        Log.e("response", res);
        JSONObject json = new JSONObject(res);
        String faceToken = json.optJSONArray("faces").optJSONObject(0).optString("face_token");
        return faceToken;
    }
    private static String creatFaceTokens(ArrayList<String> faceTokens){
        if(faceTokens == null || faceTokens.size() == 0){
            return "";
        }
        StringBuffer face = new StringBuffer();
        for (int i = 0; i < faceTokens.size(); i++){
            if(i == 0){
                face.append(faceTokens.get(i));
            }else{
                face.append(",");
                face.append(faceTokens.get(i));
            }
        }
        return face.toString();
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
}
