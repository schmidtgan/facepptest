package com.example.administrator.facepptest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.FaceSetOperate;
import com.megvii.cloud.http.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/7.
 */

public class CompareWork {

    public CompareWork(){

    };
    public  void compareResult(String imgPath1, String imgPath2, final Handler handler){
        final Bitmap bitmap1,bitmap2;
        bitmap1 = CompareWork.getresizePhoto(imgPath1);
        bitmap2 = CompareWork.getresizePhoto(imgPath2);
        final StringBuffer  sb = new StringBuffer();
        new Thread(new Runnable() {

            @Override
            public void run() {
                CommonOperate commonOperate = new CommonOperate(Constant.key, Constant.secret, false);
                FaceSetOperate FaceSet = new FaceSetOperate(Constant.key, Constant.secret, false);
                ArrayList<String> faces = new ArrayList<>();
                try {
                    Response response1 = commonOperate.detectByte(getBitmapbyte(bitmap1),0,null);
                    String faceToken1 = getFaceToken(response1);
                    faces.add(faceToken1);
                    faces.add(faceToken1);
                    Response response2= commonOperate.detectByte(getBitmapbyte(bitmap2),0,null);
                    String faceToken2 = getFaceToken(response2);
                    faces.add(faceToken2);
                    String faceTokens = creatFaceTokens(faces);
                    Response faceset = FaceSet.createFaceSet(null,"test",null,faceTokens,null, 1);
                    String faceSetResult = new String(faceset.getContent());
                    Response res = commonOperate.compare(faceToken1,null,null,null,faceToken2,null,null,null);
                    String result = new String(res.getContent());
                    Log.e("result", result);
                    String confidence =  parseJSONWithGson(result);
                    Message message = handler.obtainMessage(Constant.MSG_GOT_DATA,confidence);
                    handler.sendMessage(message);

                }catch (Exception e){
                    e.printStackTrace();
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
    private String parseJSONWithGson(String JsonData){
        Gson gson = new Gson();
        Face face = gson.fromJson(JsonData,Face.class);
        String confidence = face.getConfidence();
        Log.e("result",confidence);
        return confidence;
    }
}
