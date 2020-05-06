package com.non.sleep.naver.android.src.recommend_ai.interfaces.interfaces;


import com.non.sleep.naver.android.src.recommend.models.ObjectResponse;
import com.non.sleep.naver.android.src.recommend_ai.models.ObjectResponse2;

import java.io.InputStream;
import java.util.ArrayList;

public interface RecommendAiView {

    void retrofitFailure(String message);

    void postWordPositiveSuccess();

    void postWordNegativeSuccess();

    void postWordConfirmName(ObjectResponse objectResponse);

    void postWordConfirmCategory(ArrayList<ObjectResponse> arrayList, String word, String message);

    void postWordList(ArrayList<ObjectResponse> arrayList);
}
