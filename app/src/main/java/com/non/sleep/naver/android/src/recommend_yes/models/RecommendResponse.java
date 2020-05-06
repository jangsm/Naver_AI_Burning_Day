package com.non.sleep.naver.android.src.recommend_yes.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RecommendResponse {

    @SerializedName("isSuccess")
    boolean isSuccess;

    @SerializedName("arr")
    ArrayList<RecommendObject> recommendObjects;

    @SerializedName("code")
    int code;

    @SerializedName("message")
    String message;

    public boolean isSuccess() {
        return isSuccess;
    }

    public ArrayList<RecommendObject> getRecommendObjects() {
        return recommendObjects;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
