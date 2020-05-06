package com.non.sleep.naver.android.src.menu_list.interfaces;

import com.non.sleep.naver.android.src.menu_list.models.CommonResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MenuListRetrofitinterface {

    @POST("/naver/word")
    Call<CommonResponse> postWord(@Body RequestBody params);
}
