package com.non.sleep.naver.android.src.recommend_ai.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class WordResponse {

    @SerializedName("code")
    int code;

    @SerializedName("message")
    String message;

    @SerializedName("object")
    ObjectResponse2 object;

    @SerializedName("arr")
    ArrayList<ObjectResponse2> objectRespons2s;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ObjectResponse2 getObject() {
        return object;
    }

    public ArrayList<ObjectResponse2> getObjectRespons2s() {
        return objectRespons2s;
    }
}
