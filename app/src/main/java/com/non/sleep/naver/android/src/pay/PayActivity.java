package com.non.sleep.naver.android.src.pay;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.non.sleep.naver.android.R;
import com.non.sleep.naver.android.src.AudioWriterPCM;
import com.non.sleep.naver.android.src.BaseActivity;
import com.non.sleep.naver.android.src.NaverRecognizer;
import com.non.sleep.naver.android.src.menu_list.MenuListActivitiy;
import com.non.sleep.naver.android.src.menu_list.MenuListService;
import com.non.sleep.naver.android.src.menu_list.interfaces.MenuListView;
import com.non.sleep.naver.android.src.pay_finish.PayFinishActivity;
import com.non.sleep.naver.android.src.recommend.models.ObjectResponse;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.non.sleep.naver.android.src.ApplicationClass.arrayListSelectedMenu;

public class PayActivity extends BaseActivity implements MenuListView {


    private LinearLayout mLinearMenu1, mLinearMenu2, mLinearMenu3;
    private TextView mTextViewMenu1, mTextViewMenu2, mTextViewMenu3, mTextViewTotal;
    int price = 0;

    Context mContext;
    boolean isRecordingMode = false;
    private ImageView mImageViewRecording;
    private MediaPlayer mediaPlayer;

    private static final String TAG = PayActivity.class.getSimpleName();
    private static final String CLIENT_ID = "g0fd605ajk"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private PayActivity.RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private String mResult;
    private AudioWriterPCM writer;

    private boolean isCPVEnd = false;

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady: // 음성인식 준비 가능
//                txtResult.setText("Connected");
                System.out.println("connected");
                writer = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;
            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;
            case R.id.partialResult:
                mResult = (String) (msg.obj);
//                txtResult.setText(mResult);
                Log.d("메세지", mResult);
                break;
            case R.id.finalResult: // 최종 인식 결과
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                final List<String> results = speechRecognitionResult.getResults();
                StringBuilder strBuf = new StringBuilder();
                final ArrayList<String> similarWord = new ArrayList<>();
                for (String result : results) {
                    strBuf.append(result);
                    strBuf.append("\n");
                    similarWord.add(result);
                }
                mResult = strBuf.toString();
//                showCustomToast(mResult);
//                txtResult.setText(mResult);
//                postCPV(mResult);
//                postTest(edtTest.getText().toString(), similarWord);
                System.out.println("결과: " + results.get(0));
                startActivity(new Intent(PayActivity.this, PayFinishActivity.class));
//                postWord(results.get(0));
//                cpvTest(results.get(0));
//                new Thread(){
//                    @Override
//                    public void run() {
//                        testApi(results.get(0));
//                    }
//                }.start();
                break;
            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }
                mResult = "Error code : " + msg.obj.toString();
//                txtResult.setText(mResult);
                Glide.with(mContext).load(R.drawable.ic_speak)
                        .into(mImageViewRecording);
                isRecordingMode = false;
                break;
            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                Glide.with(mContext).load(R.drawable.ic_speak)
                        .into(mImageViewRecording);
                isRecordingMode = false;
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        mContext = this;
        init();
        handler = new PayActivity.RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(mContext, handler, CLIENT_ID);
        new Thread() {
            @Override
            public void run() {
                textToSpeech();
            }
        }.start();
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (isRecordingMode) {
                    //녹음끄기
//                    showCustomToast("dd");
                    Glide.with(mContext).load(R.drawable.ic_speak)
                            .into(mImageViewRecording);
                    isRecordingMode = false;
                } else {
                    //녹음켜기
//                    showCustomToast("dd");
                    Glide.with(mContext).asGif()
                            .load(R.raw.gif_recoding)
                            .into(mImageViewRecording);
                    isRecordingMode = true;
                    naverRecognizer.getSpeechRecognizer().initialize();
                    if (!naverRecognizer.getSpeechRecognizer().isRunning()) {
                        Log.d("로그", "루프2");
                        mResult = "";
//                        txtResult.setText("Connecting...");
                        naverRecognizer.recognize();
                    } else {
                        Log.d(TAG, "stop and wait Final Result");
                        naverRecognizer.getSpeechRecognizer().stop();
                    }
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (isCPVEnd) {
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                        isCPVEnd = false;
                        break;
                    }
                }
            }
        }.start();
    }

    void postWord(String word) {
        showProgressDialog();
        final MenuListService menuListService = new MenuListService(this);
        menuListService.postWord(word);
    }


    void init() {
        mLinearMenu1 = findViewById(R.id.linear_menu_1);
        mLinearMenu2 = findViewById(R.id.linear_menu_2);
        mLinearMenu3 = findViewById(R.id.linear_menu_3);

        mTextViewMenu1 = findViewById(R.id.pay_name1_tv);
        mTextViewMenu2 = findViewById(R.id.pay_name2_tv);
        mTextViewMenu3 = findViewById(R.id.pay_name3_tv);
        mTextViewTotal = findViewById(R.id.total_won_tv);

        mImageViewRecording = findViewById(R.id.activity_main_iv_recording);

        for (int i = 0; i < arrayListSelectedMenu.size(); i++) {
            if (i == 0) {
                mLinearMenu1.setVisibility(View.VISIBLE);
                mTextViewMenu1.setText(arrayListSelectedMenu.get(i).getName());
//                mTextViewMenu1.setText(arrayListSelectedMenu.get(i).getPrice() + "원");
            } else if (i == 1) {
                mLinearMenu2.setVisibility(View.VISIBLE);
                mTextViewMenu2.setText(arrayListSelectedMenu.get(i).getName());
//                mTextViewMenu2.setText(arrayListSelectedMenu.get(i).getPrice() + "원");
            } else {
                mLinearMenu3.setVisibility(View.VISIBLE);
                mTextViewMenu3.setText(arrayListSelectedMenu.get(i).getName());
//                mTextViewMenu2.setText(arrayListSelectedMenu.get(i).getPrice() + "원");
            }

            price += arrayListSelectedMenu.get(i).getPrice();

        }

        mTextViewTotal.setText(price + "원");

    }

    void textToSpeech() {
        String clientId = "g0fd605ajk";
        String clientSecret = "ZgiGkHGhY3kNc5ulmYD70rkKAM3FeGnONBZpjN63";
        try {
            String text = URLEncoder.encode("결제 방식을 선택해주세요.", "UTF-8"); // 13자
            String apiURL = "https://naveropenapi.apigw.ntruss.com/voice-premium/v1/tts";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
            if (responseCode == 200) { // 정상 호출
                System.out.println("성공");
                InputStream is = con.getInputStream();
                System.out.println("성공2");
                int read = 0;
                byte[] bytes = new byte[1024];
                // 랜덤한 이름으로 mp3 파일 생성
                String tempname = Long.valueOf(new Date().getTime()).toString();
                File f = new File(getFilesDir(), tempname + ".mp3");
                System.out.println("file path: " + f.getAbsolutePath());
                f.createNewFile();
                System.out.println("성공3");
                OutputStream outputStream = new FileOutputStream(f);
                System.out.println("성공4");
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                System.out.println("성공5");
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(f.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                while (mediaPlayer.isPlaying()) {
//                    Log.d("로그", "루프");
                }
                is.close();
                System.out.println("성공6");
                isCPVEnd = true;
            } else {  // 오류 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println("리스폰스 에러: " + response.toString());
            }
        } catch (Exception e) {
            System.out.println("error: " + e);
        }

    }

    private static class RecognitionHandler extends Handler {
        private final WeakReference<PayActivity> mActivity;

        RecognitionHandler(PayActivity activity) {
            mActivity = new WeakReference<PayActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PayActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    @Override
    public void retrofitFailure(String message) {
        hideProgressDialog();
        showCustomToast(message == null || message.isEmpty() ? getString(R.string.network_error) : "죄송합니다. 다시 한 번 말해주세요.");
    }

    @Override
    public void postWordPositiveSuccess() {

    }

    @Override
    public void postWordNegativeSuccess() {

    }

    @Override
    public void postWordConfirmName(ObjectResponse objectResponse) {

    }

    @Override
    public void postWordConfirmCategory(ArrayList<ObjectResponse> arrayList, String word) {

    }

    public void customOnClick(final View view) {
        switch (view.getId()) {
            case R.id.activity_main_iv_recording:
//                showCustomToast("dd");

                if (isRecordingMode) {
                    //녹음끄기
//                    showCustomToast("dd");
                    Glide.with(mContext).load(R.drawable.ic_speak)
                            .into(mImageViewRecording);
                    isRecordingMode = false;
                } else {
                    //녹음켜기
//                    showCustomToast("dd");
                    Glide.with(mContext).asGif()
                            .load(R.raw.gif_recoding)
                            .into(mImageViewRecording);
                    isRecordingMode = true;
                    if (!naverRecognizer.getSpeechRecognizer().isRunning()) {
                        Log.d("로그", "루프2");
                        mResult = "";
//                        txtResult.setText("Connecting...");
                        naverRecognizer.recognize();
                    } else {
                        Log.d(TAG, "stop and wait Final Result");
                        naverRecognizer.getSpeechRecognizer().stop();
                    }

                }
                break;
        }
    }

}
