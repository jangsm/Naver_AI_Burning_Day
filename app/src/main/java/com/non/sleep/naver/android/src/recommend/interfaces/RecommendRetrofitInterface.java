package com.non.sleep.naver.android.src.recommend.interfaces;

import com.non.sleep.naver.android.src.recommend.models.WordResponse;
import com.non.sleep.naver.android.src.recommend_yes.models.RecommendResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RecommendRetrofitInterface {

    @POST("/naver/word")
    Call<WordResponse> postWord(@Body RequestBody params);
}
