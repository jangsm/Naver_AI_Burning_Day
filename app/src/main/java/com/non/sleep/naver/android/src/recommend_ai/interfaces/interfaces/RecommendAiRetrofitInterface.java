package com.non.sleep.naver.android.src.recommend_ai.interfaces.interfaces;


import com.non.sleep.naver.android.src.menu_list.models.CommonResponse;
import com.non.sleep.naver.android.src.recommend_ai.models.WordResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RecommendAiRetrofitInterface {

    @POST("/naver/word")
    Call<CommonResponse> postWord(@Body RequestBody params);

    @POST("/naver/recommend")
    Call<CommonResponse> postWordList(@Body RequestBody params);
}
