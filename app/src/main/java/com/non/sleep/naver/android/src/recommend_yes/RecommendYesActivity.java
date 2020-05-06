package com.non.sleep.naver.android.src.recommend_yes;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.non.sleep.naver.android.R;
import com.non.sleep.naver.android.src.AudioWriterPCM;
import com.non.sleep.naver.android.src.BaseActivity;
import com.non.sleep.naver.android.src.NaverRecognizer;
import com.non.sleep.naver.android.src.main.MainActivity;
import com.non.sleep.naver.android.src.menu_list.MenuListActivitiy;
import com.non.sleep.naver.android.src.recommend.RecommendActivity;
import com.non.sleep.naver.android.src.recommend.RecommendService;
import com.non.sleep.naver.android.src.recommend_yes.interfaces.RecommendYesView;
import com.non.sleep.naver.android.src.recommend_yes.models.RecommendObject;
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

public class RecommendYesActivity extends BaseActivity implements RecommendYesView {

    Context mContext;
    boolean isRecordingMode = false;
    private ImageView mImageViewRecording, mImageViewMenu;
    private MediaPlayer mediaPlayer;

    private static final String TAG = RecommendYesActivity.class.getSimpleName();
    private static final String CLIENT_ID = "g0fd605ajk"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private RecommendYesActivity.RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private String mResult;
    private AudioWriterPCM writer;

    int age;
    String gender = "M";

    String name;
    int price;
    int menuNo;
    private TextView mTextViewAge;
    private TextView mTextViewFoodName;

    private boolean isCPVEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_yes);
        mContext = this;
        init();
        handler = new RecommendYesActivity.RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(mContext, handler, CLIENT_ID);
        new Thread() {
            @Override
            public void run() {
                textToSpeech(age);
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
//                        Log.d("로그", "루프2");
                        mResult = "";
//                        txtResult.setText("Connecting...");
                        naverRecognizer.recognize();
                    } else {
//                        Log.d(TAG, "stop and wait Final Result");
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

    @Override
    protected void onStart() {
        super.onStart();
        naverRecognizer.getSpeechRecognizer().initialize();
    }

    void init() {
        age = getIntent().getIntExtra("age", 20);
        gender = getIntent().getStringExtra("gender");
        postRecommend(age, gender);
        mImageViewRecording = findViewById(R.id.yes_iv_recording);
        mTextViewAge = findViewById(R.id.food_age);
        mTextViewAge.setText(age + "대 추천 메뉴");
        mTextViewFoodName = findViewById(R.id.food_name);

        mImageViewMenu = findViewById(R.id.iv_menu);
    }

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
//                System.out.println("결과: " + results.get(0));
                postWord(results.get(0));
                Log.d("메세지 post word", results.get(0));
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
                Glide.with(RecommendYesActivity.this).load(R.drawable.ic_speak)
                        .into(mImageViewRecording);
                isRecordingMode = false;
                break;
            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                Glide.with(RecommendYesActivity.this).load(R.drawable.ic_speak)
                        .into(mImageViewRecording);
                isRecordingMode = false;
                break;
        }
    }

    private static class RecognitionHandler extends Handler {
        private final WeakReference<RecommendYesActivity> mActivity;

        RecognitionHandler(RecommendYesActivity activity) {
            mActivity = new WeakReference<RecommendYesActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RecommendYesActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


    }

    @Override
    public void onStop() {
        super.onStop();
        naverRecognizer.getSpeechRecognizer().release();
    }

    void textToSpeech(int age) {
        String clientId = "g0fd605ajk";
        String clientSecret = "ZgiGkHGhY3kNc5ulmYD70rkKAM3FeGnONBZpjN63";
        try {
            String text = URLEncoder.encode(age + "대 추천 메뉴입니다.   선택하시겠습니까?", "UTF-8"); // 13자
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

    void postRecommend(int age, String gender) {
        System.out.println("age: " + age + ", " + gender);
        showProgressDialog();
        final RecommendYesService recommendYesService = new RecommendYesService(this);
        recommendYesService.postRecommend(age, gender);

    }

    @Override
    public void retrofitFailure(String message) {
        hideProgressDialog();
        showCustomToast(message == null || message.isEmpty() ? getString(R.string.network_error) : "죄송합니다. 다시 한 번 말해주세요.");
    }

    @Override
    public void postRecommendSuccess(ArrayList<RecommendObject> arrayList) {
        hideProgressDialog();
        Log.d("name", arrayList.get(0).getName());
        String percent = Double.toString(arrayList.get(0).getPercentage());
        percent = percent.substring(0, percent.indexOf('.') + 2);
        mTextViewFoodName.setText(arrayList.get(0).getName() + " (" + percent + "%) ");
        name = arrayList.get(0).getName();
        price = arrayList.get(0).getPrice();
        menuNo = arrayList.get(0).getMenuNo();
        Glide.with(RecommendYesActivity.this).load(arrayList.get(0).getImageUrl())
                .into(mImageViewMenu);
    }

    void postWord(String word) {
//        showProgressDialog();
        final RecommendYesService recommendService = new RecommendYesService(this);
        recommendService.postWord(word);
    }

    @Override
    public void yes() {
        yesClick();
    }

    @Override
    public void no() {
        noClick();
    }

    public void customOnClick(final View view) {
        switch (view.getId()) {
            case R.id.yes_iv_recording:
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
                    naverRecognizer.getSpeechRecognizer().initialize();
                    if (!naverRecognizer.getSpeechRecognizer().isRunning()) {
//                        Log.d("로그", "루프3333");
                        mResult = "";
//                        txtResult.setText("Connecting...");
                        naverRecognizer.recognize();
                    } else {
//                        Log.d(TAG, "stop and wait Final Result");
                        naverRecognizer.getSpeechRecognizer().stop();
                    }

                }
                break;

            case R.id.recommend_iv_yes:
                Intent intent = new Intent(this, ShoppingActivity.class);
                intent.putExtra("age", age);
                intent.putExtra("gender", gender);
                startActivity(intent);
                finish();
                break;

            case R.id.recommend_iv_no:
                intent = new Intent(this, MenuListActivitiy.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    void yesClick() {
        arrayListSelectedMenu.add(new selectedMenu(name, price, menuNo));
        Log.d("로그", name+price);
        Intent intent = new Intent(this, ShoppingActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("price", price);
        startActivity(intent);
    }

    void noClick() {
        Intent intent = new Intent(this, MenuListActivitiy.class);
        startActivity(intent);
    }
}
