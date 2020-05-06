package com.non.sleep.naver.android.src.shopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.bumptech.glide.Glide;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.non.sleep.naver.android.R;
import com.non.sleep.naver.android.src.AudioWriterPCM;
import com.non.sleep.naver.android.src.BaseActivity;
import com.non.sleep.naver.android.src.NaverRecognizer;
import com.non.sleep.naver.android.src.menu_list.MenuListActivitiy;
import com.non.sleep.naver.android.src.menu_list.MenuListService;
import com.non.sleep.naver.android.src.menu_list.interfaces.MenuListView;
import com.non.sleep.naver.android.src.pay.PayActivity;
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

public class ShoppingActivity extends BaseActivity implements MenuListView {
    private TextView mTvName1, mTvName2, mTvName3, mTvWon1, mTvWon2, mTvWon3;

    Context mContext;
    boolean isRecordingMode = false;
    private ImageView mImageViewRecording;
    private MediaPlayer mediaPlayer;

    private static final String TAG = ShoppingActivity.class.getSimpleName();
    private static final String CLIENT_ID = "g0fd605ajk"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private ShoppingActivity.RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private String mResult;
    private AudioWriterPCM writer;

    private LinearLayout mLinearMenu1, mLinearMenu2, mLinearMenu3;

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
                postWord(results.get(0));
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
        setContentView(R.layout.activity_shopping);
        mContext = this;

        String name1 = getIntent().getStringExtra("name1");
        String name2 = getIntent().getStringExtra("name2");
        String name3 = getIntent().getStringExtra("name3");

        int won1 = getIntent().getIntExtra("won1", 0);
        int won2 = getIntent().getIntExtra("won2", 0);
        int won3 = getIntent().getIntExtra("won3", 0);

        mTvName1 = findViewById(R.id.shop_tv_menu1);
        mTvName2 = findViewById(R.id.shop_tv_menu2);
        mTvName3 = findViewById(R.id.shop_tv_menu3);

        mTvWon1 = findViewById(R.id.shop_tv_price1);
        mTvWon2 = findViewById(R.id.shop_tv_price2);
        mTvWon3 = findViewById(R.id.shop_tv_price3);

        mLinearMenu1 = findViewById(R.id.linear_menu_1);
        mLinearMenu2 = findViewById(R.id.linear_menu_2);
        mLinearMenu3 = findViewById(R.id.linear_menu_3);

        for (int i = 0; i < arrayListSelectedMenu.size(); i++) {
            if (i == 0) {
                mLinearMenu1.setVisibility(View.VISIBLE);
                mTvName1.setText(arrayListSelectedMenu.get(i).getName());
//                mTvWon1.setText(arrayListSelectedMenu.get(i).getPrice() + "원");
            } else if (i == 1) {
                mLinearMenu2.setVisibility(View.VISIBLE);
                mTvName2.setText(arrayListSelectedMenu.get(i).getName());
//                mTvWon2.setText(arrayListSelectedMenu.get(i).getPrice() + "원");
            } else {
                mLinearMenu3.setVisibility(View.VISIBLE);
                mTvName3.setText(arrayListSelectedMenu.get(i).getName());
//                mTvWon3.setText(arrayListSelectedMenu.get(i).getPrice() + "원");
            }

        }


        mImageViewRecording = findViewById(R.id.activity_main_iv_recording);

        if (name1 != null && name1 != "") {
            mTvName1.setText(name1);
        }
        if (name2 != null && name2 != "") {
            mTvName2.setText(name2);
        }
        if (name3 != null && name3 != "") {
            mTvName3.setText(name3);
        }

//        mTvWon1.setText(won1 + "원");
//        mTvWon2.setText(won2 + "원");
//        mTvWon3.setText(won3 + "원");

        handler = new ShoppingActivity.RecognitionHandler(this);
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

    @Override
    public void retrofitFailure(String message) {
        hideProgressDialog();
        showCustomToast(message == null || message.isEmpty() ? getString(R.string.network_error) : "죄송합니다. 다시 한 번 말해주세요.");
    }

    @Override
    public void postWordPositiveSuccess() {
        hideProgressDialog();
        Intent intent = new Intent(this, MenuListActivitiy.class);
        startActivity(intent);
        System.out.println("리스폰스 코드: 1");
    }

    @Override
    public void postWordNegativeSuccess() {
        hideProgressDialog();

        Intent intent = new Intent(this, PayActivity.class);
        startActivity(intent);

        System.out.println("리스폰스 코드: 2");
    }

    @Override
    public void postWordConfirmName(ObjectResponse objectResponse) {
        hideProgressDialog();
        System.out.println("리스폰스 코드: 3");
    }

    @Override
    public void postWordConfirmCategory(ArrayList<ObjectResponse> arrayList, String word) {
        hideProgressDialog();
        System.out.println("리스폰스 코드: 4");
    }

    private static class RecognitionHandler extends Handler {
        private final WeakReference<ShoppingActivity> mActivity;

        RecognitionHandler(ShoppingActivity activity) {
            mActivity = new WeakReference<ShoppingActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ShoppingActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        naverRecognizer.getSpeechRecognizer().initialize();
    }

    @Override
    public void onStop() {
        super.onStop();
        naverRecognizer.getSpeechRecognizer().release();
    }

    public void customOnClick(View view) {
        switch (view.getId()) {
            case R.id.shopping_yes_iv:
                break;
            case R.id.shopping_no_iv:
                break;
            case R.id.activity_main_iv_recording:
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
                break;
        }
    }

    void textToSpeech() {
        String clientId = "g0fd605ajk";
        String clientSecret = "ZgiGkHGhY3kNc5ulmYD70rkKAM3FeGnONBZpjN63";
        try {
            String text = URLEncoder.encode("추가 주문 하시겠습니까?", "UTF-8"); // 13자
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
}