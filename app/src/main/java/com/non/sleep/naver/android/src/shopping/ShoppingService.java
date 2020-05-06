package com.non.sleep.naver.android.src.shopping;

import android.util.Log;

import com.non.sleep.naver.android.src.recommend.interfaces.RecommendRetrofitInterface;
import com.non.sleep.naver.android.src.recommend.models.WordResponse;
import com.non.sleep.naver.android.src.recommend_yes.interfaces.RecommendYesRetrofitInterface;
import com.non.sleep.naver.android.src.recommend_yes.interfaces.RecommendYesView;
import com.non.sleep.naver.android.src.recommend_yes.models.RecommendResponse;
import com.non.sleep.naver.android.src.shopping.interfaces.ShoppingView;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.non.sleep.naver.android.src.ApplicationClass.MEDIA_TYPE_JSON;
import static com.non.sleep.naver.android.src.ApplicationClass.getRetrofit;

public class ShoppingService {

    private final ShoppingView mRecommendYesView;

    ShoppingService(ShoppingView shoppingView) {
        mRecommendYesView = shoppingView;
    }

    void postWord(String word) {
        JSONObject params = new JSONObject();
        try {
            params.put("word", word);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RecommendRetrofitInterface recommendRetrofitInterface = getRetrofit().create(RecommendRetrofitInterface.class);
        recommendRetrofitInterface.postWord(RequestBody.create(params.toString(), MEDIA_TYPE_JSON)).enqueue(new Callback<WordResponse>() {
            @Override
            public void onResponse(Call<WordResponse> call, Response<WordResponse> response) {
                final WordResponse wordResponse = response.body();
                if (wordResponse == null) {
                    mRecommendYesView.retrofitFailure(null);
                } else if (wordResponse.getCode() == 1 || wordResponse.getCode() == 11) {
                    mRecommendYesView.yes();
                } else if (wordResponse.getCode() == 2 || wordResponse.getCode() == 22) {
                    mRecommendYesView.no();
                } else {
                    mRecommendYesView.retrofitFailure(wordResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<WordResponse> call, Throwable t) {
                mRecommendYesView.retrofitFailure(null);
            }
        });
    }
}
