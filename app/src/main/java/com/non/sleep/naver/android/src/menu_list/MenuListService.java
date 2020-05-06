package com.non.sleep.naver.android.src.menu_list;

import com.non.sleep.naver.android.src.menu_list.interfaces.MenuListRetrofitinterface;
import com.non.sleep.naver.android.src.menu_list.interfaces.MenuListView;
import com.non.sleep.naver.android.src.menu_list.models.CommonResponse;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.non.sleep.naver.android.src.ApplicationClass.MEDIA_TYPE_JSON;
import static com.non.sleep.naver.android.src.ApplicationClass.getRetrofit;

public class MenuListService {

    private final MenuListView mMenuListView;

    public MenuListService(MenuListView menuListView){
        mMenuListView = menuListView;
    }

    public void postWord(final String word){
        JSONObject params = new JSONObject();
        try {
            params.put("word",word);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final MenuListRetrofitinterface menuListRetrofitinterface = getRetrofit().create(MenuListRetrofitinterface.class);
        menuListRetrofitinterface.postWord(RequestBody.create(params.toString(),MEDIA_TYPE_JSON)).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                final CommonResponse commonResponse = response.body();
                if(commonResponse==null){
                    mMenuListView.retrofitFailure(null);
                }
                else if(commonResponse.getCode()==1){ // 긍정
                    mMenuListView.postWordPositiveSuccess();
                }
                else if(commonResponse.getCode()==2){ // 부정
                    mMenuListView.postWordNegativeSuccess();
                }
                else if(commonResponse.getCode()==3){ // 메뉴이름 확정
                    mMenuListView.postWordConfirmName(commonResponse.getObject());
                }
                else if(commonResponse.getCode()==4){ // 메뉴카테고리
                    mMenuListView.postWordConfirmCategory(commonResponse.getObjectResponses(), word);
                }
//                else if(commonResponse.getCode()==12){ // 현금
//                    mMenuListView.postWordCashSuccess();
//                }
//                else if(commonResponse.getCode()==13){
//                    mMenuListView.postWordCardSuccess();
//                }
                else{
                    mMenuListView.retrofitFailure(commonResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                mMenuListView.retrofitFailure(null);
            }
        });
    }
}
