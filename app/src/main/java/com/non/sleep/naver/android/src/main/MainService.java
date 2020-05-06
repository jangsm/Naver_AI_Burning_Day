package com.non.sleep.naver.android.src.main;

import android.media.MediaPlayer;

import com.non.sleep.naver.android.src.main.interfaces.MainActivityView;
import com.non.sleep.naver.android.src.main.interfaces.MainRetrofitInterface;
import com.non.sleep.naver.android.src.main.models.DefaultResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.non.sleep.naver.android.src.ApplicationClass.MEDIA_TYPE_JSON;
import static com.non.sleep.naver.android.src.ApplicationClass.getRetrofit;


class MainService {
    private final MainActivityView mMainActivityView;

    MainService(final MainActivityView mainActivityView) {
        this.mMainActivityView = mainActivityView;
    }

    void cpv(String input){
        String clientId = "g0fd605ajk";
        String clientSecret = "ZgiGkHGhY3kNc5ulmYD70rkKAM3FeGnONBZpjN63";
        try {
            String text = URLEncoder.encode(input, "UTF-8"); // 13자
            String apiURL = "https://naveropenapi.apigw.ntruss.com/voice-premium/v1/tts";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
            // post request
            String postParams = "speaker=nara&volume=0&speed=0&pitch=0&emotion=0&format=mp3&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                System.out.println("성공");
                InputStream is = con.getInputStream();
                mMainActivityView.cpvSuccess(is);
                System.out.println("성공2");
            } else {  // 오류 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                mMainActivityView.validateFailure(response.toString());
                System.out.println("리스폰스 에러: " + response.toString());
            }
        } catch (Exception e) {
            mMainActivityView.validateFailure(null);
            System.out.println("error: " + e);
        }
    }

    void postTest(String word, ArrayList<String> similarWord){
        JSONObject params = new JSONObject();
        try {
            params.put("word", word);
            JSONArray array = new JSONArray();
            for(int i=0; i<similarWord.size(); i++){
                JSONObject object = new JSONObject();
                object.put("name", similarWord.get(i));
                array.put(object);
            }
            params.put("similarWord", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final MainRetrofitInterface mainRetrofitInterface = getRetrofit().create(MainRetrofitInterface.class);
        mainRetrofitInterface.postTest(RequestBody.create(params.toString(), MEDIA_TYPE_JSON)).enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                final DefaultResponse defaultResponse = response.body();
                if(defaultResponse==null){
                    mMainActivityView.validateFailure(null);
                }
                else if(defaultResponse.getCode() == 100){
                    mMainActivityView.validateSuccess("성공");
                }
                else{
                    mMainActivityView.validateFailure(defaultResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {
                mMainActivityView.validateFailure(null);
                System.out.println("에러: " +t.toString());
            }
        });
    }
}
