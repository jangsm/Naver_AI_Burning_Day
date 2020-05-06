package com.non.sleep.naver.android.config;

import androidx.annotation.NonNull;

import com.non.sleep.naver.android.R;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.non.sleep.naver.android.src.ApplicationClass.X_ACCESS_TOKEN;
import static com.non.sleep.naver.android.src.ApplicationClass.sSharedPreferences;


public class XAccessTokenInterceptor implements Interceptor {

    @Override
    @NonNull
    public Response intercept(@NonNull final Interceptor.Chain chain) throws IOException {
        final Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("X-NCP-APIGW-API-KEY-ID", String.valueOf(R.string.naverId));
        builder.addHeader("X-NCP-APIGW-API-KEY", String.valueOf(R.string.naverKey));
        builder.addHeader("Content-Type", "application/x-www-form-urlencoded");

//        final String jwtToken = sSharedPreferences.getString(X_ACCESS_TOKEN, null);
//        if (jwtToken != null) {
//
//        }
        return chain.proceed(builder.build());
    }
}
