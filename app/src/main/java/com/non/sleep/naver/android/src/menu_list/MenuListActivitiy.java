package com.non.sleep.naver.android.src.menu_list;

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
import com.non.sleep.naver.android.src.menu_list.interfaces.MenuListView;
import com.non.sleep.naver.android.src.recommend.models.ObjectResponse;
import com.non.sleep.naver.android.src.recommend_ai.RecommendAiActivity;
import com.non.sleep.naver.android.src.recommend_yes.RecommendYesActivity;
import com.non.sleep.naver.android.src.selectedMenu;
import com.non.sleep.naver.android.src.shopping.ShoppingActivity;

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

public class MenuListActivitiy extends BaseActivity implements MenuListView {

    Context mContext;
    boolean isRecordingMode = false;
    private ImageView mImageViewRecording;
    private MediaPlayer mediaPlayer;

    private static final String TAG = MenuListActivitiy.class.getSimpleName();
    private static final String CLIENT_ID = "g0fd605ajk"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private MenuListActivitiy.RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private String mResult;
    private AudioWriterPCM writer;
    private LinearLayout mLinearMenu1, mLinearMenu2, mLinearMenu3;
    private TextView mTextViewMenu1, mTextViewMenu2, mTextViewMenu3;

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
        setContentView(R.layout.activity_menu_list_activitiy);
        mContext = this;
        init();
        handler = new MenuListActivitiy.RecognitionHandler(this);
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

    void init() {
        mImageViewRecording = findViewById(R.id.activity_main_iv_recording);
        mLinearMenu1 = findViewById(R.id.linear_menu_1);
        mLinearMenu2 = findViewById(R.id.linear_menu_2);
        mLinearMenu3 = findViewById(R.id.linear_menu_3);

        mTextViewMenu1 = findViewById(R.id.tv_menu1);
        mTextViewMenu2 = findViewById(R.id.tv_menu2);
        mTextViewMenu3 = findViewById(R.id.tv_menu3);

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
                mTextViewMenu2.setText(arrayListSelectedMenu.get(i).getName());
//                mTextViewMenu2.setText(arrayListSelectedMenu.get(i).getPrice() + "원");
            }

        }

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
        System.out.println("리스폰스 코드: 1");
    }

    @Override
    public void postWordNegativeSuccess() {
        hideProgressDialog();
        System.out.println("리스폰스 코드: 2");
    }

    @Override
    public void postWordConfirmName(ObjectResponse objectResponse) {
        hideProgressDialog();
        System.out.println("리스폰스 코드: 3");
        arrayListSelectedMenu.add(new selectedMenu(objectResponse.getName(), 1000, objectResponse.getMenuNo()));
        Intent intent = new Intent(MenuListActivitiy.this, ShoppingActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void postWordConfirmCategory(ArrayList<ObjectResponse> arrayList, String word) {
        hideProgressDialog();
        System.out.println("리스폰스 코드: 4");
        Intent intent = new Intent(MenuListActivitiy.this, RecommendAiActivity.class);
        intent.putExtra("type", word);
        startActivity(intent);
        finish();
    }

    private static class RecognitionHandler extends Handler {
        private final WeakReference<MenuListActivitiy> mActivity;

        RecognitionHandler(MenuListActivitiy activity) {
            mActivity = new WeakReference<MenuListActivitiy>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MenuListActivitiy activity = mActivity.get();
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

    void textToSpeech() {
        String clientId = "g0fd605ajk";
        String clientSecret = "ZgiGkHGhY3kNc5ulmYD70rkKAM3FeGnONBZpjN63";
        try {
            String text = URLEncoder.encode("어떤 메뉴를 선택하시겠습니까?", "UTF-8"); // 13자
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

    public void customOnClick(View view) {
        switch (view.getId()) {
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

            case R.id.iv_gimbap:
                arrayListSelectedMenu.add(new selectedMenu("야채김밥", 1000, 1));
                break;
            case R.id.iv_gimchi:
                arrayListSelectedMenu.add(new selectedMenu("김치찌개", 1000, 9));
                break;
            case R.id.iv_udong:
                arrayListSelectedMenu.add(new selectedMenu("우동", 1000, 4));
                break;
            case R.id.iv_bibimbap:
                arrayListSelectedMenu.add(new selectedMenu("돌솥비빔밥", 1000, 8));
                break;
        }
    }
}
