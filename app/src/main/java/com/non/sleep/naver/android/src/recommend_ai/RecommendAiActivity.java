package com.non.sleep.naver.android.src.recommend_ai;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import com.non.sleep.naver.android.src.menu_list.MenuListActivitiy;
import com.non.sleep.naver.android.src.recommend.models.ObjectResponse;
import com.non.sleep.naver.android.src.recommend_ai.interfaces.interfaces.RecommendAiView;
import com.non.sleep.naver.android.src.recommend_ai.models.ObjectResponse2;
import com.non.sleep.naver.android.src.recommend_yes.RecommendYesActivity;
import com.non.sleep.naver.android.src.selectedMenu;
import com.non.sleep.naver.android.src.shopping.ShoppingActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class RecommendAiActivity extends BaseActivity implements RecommendAiView {

    private Context mContext;

    private MediaPlayer mediaPlayer;

    private Button mButtonYes;

    private static final String TAG = RecommendAiActivity.class.getSimpleName();
    private static final String CLIENT_ID = "g0fd605ajk"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private RecommendAiActivity.RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private TextView txtResult;
    private Button btnStart;
    private String mResult;
    private AudioWriterPCM writer;
    private ImageView mImageViewRecording;
    boolean isRecordingMode = false;

    private RecyclerView mRV;
    private ArrayList<ObjectResponse> arrayList = new ArrayList<>();
    public static TextView mTvName1, mTvName2, mTvName3, mTvName4, mTvWon1, mTvWon2, mTvWon3, mTvWon4, mTvType, mTvTitle;
    private RecommendAiAdapter adapter;
    private boolean isCPVEnd = false;

    public static int count=0;
    public static int won=0;
    public static TextView mTvTotalPay;

    private Intent intent;

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
                for(String result : results) {
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

    static class RecognitionHandler extends Handler {
        private final WeakReference<RecommendAiActivity> mActivity;
        RecognitionHandler(RecommendAiActivity activity) {
            mActivity = new WeakReference<RecommendAiActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            RecommendAiActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    void postWord(String word){
//        showProgressDialog();
        final RecommendAiService recommendAiService = new RecommendAiService(this);
        recommendAiService.postWord(word);
    }

//    void postWorldListFun(int age, String gender) {
//        showProgressDialog();
//        final RecommendAiService recommendAiService = new RecommendAiService(this);
//        recommendAiService.postWordList(age, gender);
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_recommend);
        mImageViewRecording = findViewById(R.id.activity_main_iv_recording);
        mContext = this;
        permissionCheck();
        init();
        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, CLIENT_ID);

        String type = getIntent().getStringExtra("type");
        postWord(type);

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(isRecordingMode){
                    //녹음끄기
//                    showCustomToast("dd");
                    Glide.with(mContext).load(R.drawable.ic_speak)
                            .into(mImageViewRecording);
                    isRecordingMode = false;
                }
                else {
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
        new Thread(){
            @Override
            public void run() {
                while (true){
                    if(isCPVEnd){
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                        isCPVEnd = false;
                        break;
                    }
                }
            }
        }.start();

//        int age = getIntent().getIntExtra("age", 0);
//        String gender = getIntent().getStringExtra("gender");
//        mTvType.setText(type + " 추천해줘");


        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        if(mRV == null) {
            Log.i("SDVSVD", "SDVSVD");
        }
        mRV.setLayoutManager(mLayoutManager);
        adapter = new RecommendAiAdapter(mContext, new RecommendAiAdapter.RecommendListener() {
            @Override
            public void itemClick(int pos, String name, int menuNo) {
                killMediaPlayer();
                arrayListSelectedMenu.add(new selectedMenu(name, 1000, menuNo));
                Intent intent = new Intent(RecommendAiActivity.this, ShoppingActivity.class);
                startActivity(intent);
            }
        });
        mRV.setAdapter(adapter);

//        postWorldListFun(20, "F");

//
    }



    public void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }


    void init(){
        mTvName1 = findViewById(R.id.recommend_ai_name1_tv);
        mTvName2 = findViewById(R.id.recommend_ai_name2_tv);
        mTvName3 = findViewById(R.id.recommend_ai_name3_tv);
        mTvName4 = findViewById(R.id.recommend_ai_name4_tv);
        mTvWon1 = findViewById(R.id.recommend_ai_won1_tv);
        mTvWon2 = findViewById(R.id.recommend_ai_won2_tv);
        mTvWon3 = findViewById(R.id.recommend_ai_won3_tv);
        mTvWon4 = findViewById(R.id.recommend_ai_won4_tv);
        mTvType = findViewById(R.id.recommend_ai_type_tv);
        mTvTotalPay = findViewById(R.id.recommend_ai_won_tv);
        mRV = findViewById(R.id.recommend_ai_rv);
//        mButtonYes = findViewById(R.id.recommend_btn_yes);
//        mButtonYes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                killMediaPlayer();
//                Intent intent = new Intent(RecommendActivity.this, RecommendYesActivity.class);
//                intent.putExtra("age", 20);
//                intent.putExtra("gender","F");
//                startActivity(intent);
//            }
//        });
//        txtResult = findViewById(R.id.recommend_tv_test);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResult = "";
//        txtResult.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        naverRecognizer.getSpeechRecognizer().initialize();
    }





    @Override
    public void retrofitFailure(String message) {
        hideProgressDialog();
        showCustomToast(message == null || message.isEmpty() ? getString(R.string.network_error) : "죄송합니다. 다시 한 번 말해주세요.");
    }

    @Override
    public void postWordPositiveSuccess() {
        hideProgressDialog();
        Intent intent = new Intent(RecommendAiActivity.this, RecommendYesActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void postWordNegativeSuccess() {

    }

    @Override
    public void postWordConfirmName(ObjectResponse objectResponse) {
        arrayListSelectedMenu.add(new selectedMenu(objectResponse.getName(), 1000, objectResponse.getMenuNo()));
        Intent intent = new Intent(RecommendAiActivity.this, ShoppingActivity.class);
        startActivity(intent);
    }

    @Override
    public void postWordConfirmCategory(ArrayList<ObjectResponse> arrayList, String word, final String message) {
        System.out.println(message);
        mTvType.setText(message);
        new Thread(){
            @Override
            public void run() {
                textToSpeech(message);
            }
        }.start();
        this.arrayList = arrayList;
        adapter.mData = arrayList;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void postWordList(ArrayList<ObjectResponse> list) {
        Log.i("SDVsd", "SDVDS");
        adapter.mData = list;
        adapter.notifyDataSetChanged();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mediaPlayer!=null){
            try{
                mediaPlayer.release();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    void textToSpeech(String input) {
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
                while ((read =is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                System.out.println("성공5");
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(f.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                while (mediaPlayer.isPlaying()){
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

    void killMediaPlayer(){
        if(mediaPlayer!=null){
            try{
                mediaPlayer.release();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        naverRecognizer.getSpeechRecognizer().release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
    }

    public void customOnClick(final View view) {
        switch (view.getId()) {
            case R.id.activity_main_iv_recording:
//                showCustomToast("dd");

                if(isRecordingMode){
                    //녹음끄기
//                    showCustomToast("dd");
                    Glide.with(mContext).load(R.drawable.ic_speak)
                            .into(mImageViewRecording);
                    isRecordingMode = false;
                }
                else{
                    //녹음켜기
//                    showCustomToast("dd");
                    Glide.with(mContext).asGif()
                            .load(R.raw.gif_recoding)
                            .into(mImageViewRecording);
                    isRecordingMode = true;
                    if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
                        Log.d("로그", "루프2");
                        mResult = "";
//                        txtResult.setText("Connecting...");
                        naverRecognizer.recognize();
                    }
                    else {
                        Log.d(TAG, "stop and wait Final Result");
                        naverRecognizer.getSpeechRecognizer().stop();
                    }

                }
                break;
            case R.id.recommend_ai_cancel_btn:
                intent = new Intent(RecommendAiActivity.this, MenuListActivitiy.class);
//                intent.putExtra("",age);
//                intent.putExtra("gender",gender);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.recommend_ai_pay_iv:
//                intent = new Intent(RecommendAiActivity.this, PayActivity.class);
//                intent.putExtra("name1", mTvName1.getText());
//                intent.putExtra("name2", mTvName2.getText());
//                intent.putExtra("name3", mTvName3.getText());
//
//                intent.putExtra("won1", mTvWon1.getText());
//                intent.putExtra("won2", mTvWon2.getText());
//                intent.putExtra("won3", mTvWon3.getText());
//                startActivity(intent);
//                finish();
                break;
        }
    }
}
