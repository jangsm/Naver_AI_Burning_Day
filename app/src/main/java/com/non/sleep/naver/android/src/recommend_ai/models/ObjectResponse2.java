package com.non.sleep.naver.android.src.recommend_ai.models;

import com.google.gson.annotations.SerializedName;

public class ObjectResponse2 {
    @SerializedName("menuNo")
    int menuNo;

    public void setMenuNo(int menuNo) {
        this.menuNo = menuNo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @SerializedName("name")
    String name;
    @SerializedName("imageUrl")
    String imageUrl;
    @SerializedName("content")
    String content;
    @SerializedName("price")
    int price;
    @SerializedName("category")
    String category;

    public int getMenuNo() {
        return menuNo;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getContent() {
        return content;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public boolean select = false;
}
