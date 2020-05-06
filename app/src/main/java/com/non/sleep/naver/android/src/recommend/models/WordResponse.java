package com.non.sleep.naver.android.src.recommend.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class WordResponse {

    @SerializedName("code")
    int code;

    @SerializedName("message")
    String message;

    @SerializedName("object")
    ObjectResponse object;

    @SerializedName("array")
    ArrayList<ObjectResponse> objectResponses;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ObjectResponse getObject() {
        return object;
    }

    public ArrayList<ObjectResponse> getObjectResponses() {
        return objectResponses;
    }
}
