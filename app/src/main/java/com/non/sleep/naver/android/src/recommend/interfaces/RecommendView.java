package com.non.sleep.naver.android.src.recommend.interfaces;

import java.io.InputStream;

public interface RecommendView {

    void cpvFailure(String message);

    void cpvSuccess(InputStream inputStream);

    void retrofitFailure(String message);

    void postWordPositiveSuccess();

    void postWordNegativeSuccess();


}
