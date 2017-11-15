package com.example.administrator.facepptest;

/**
 * Created by Administrator on 2017/11/7.
 */

public class Face {
    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    private String confidence;

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    private String request_id;
}
