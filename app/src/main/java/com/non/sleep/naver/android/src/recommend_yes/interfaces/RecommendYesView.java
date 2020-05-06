package com.non.sleep.naver.android.src.recommend_yes.interfaces;

import com.non.sleep.naver.android.src.recommend_yes.models.RecommendObject;

import java.util.ArrayList;

public interface RecommendYesView {

    void retrofitFailure(String message);

    void postRecommendSuccess(ArrayList<RecommendObject> arrayList);

    void yes();

    void no();

}
