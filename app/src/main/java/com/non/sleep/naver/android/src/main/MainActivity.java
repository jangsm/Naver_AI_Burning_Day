package com.non.sleep.naver.android.src.main;

import android.Manifest;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionException;
import com.naver.speech.clientapi.SpeechRecognitionListener;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.naver.speech.clientapi.SpeechRecognizer;
import com.non.sleep.naver.android.R;
import com.non.sleep.naver.android.src.AudioWriterPCM;
import com.non.sleep.naver.android.src.BaseActivity;
import com.non.sleep.naver.android.src.NaverRecognizer;
import com.non.sleep.naver.android.src.main.interfaces.MainActivityView;

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


public class MainActivity extends BaseActivity implements MainActivityView {
    private TextView mTvHelloWorld;
    private EditText edtTest;
    private Context mContext;

    private MediaPlayer mediaPlayer;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CLIENT_ID = "g0fd605ajk"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private TextView txtResult;
    private Button btnStart;
    private String mResult;
    private AudioWriterPCM writer;



    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady: // 음성인식 준비 가능
                txtResult.setText("Connected");
                writer = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;
            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;
            case R.id.partialResult:
                mResult = (String) (msg.obj);
                txtResult.setText(mResult);
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
                txtResult.setText(mResult);
//                postTest(edtTest.getText().toString(), similarWord);
                System.out.println("결과: " + results.get(0));
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
                txtResult.setText(mResult);
                btnStart.setText(R.string.str_start);
                btnStart.setEnabled(true);
                break;
            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                btnStart.setText(R.string.str_start);
                btnStart.setEnabled(true);
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        permissionCheck();

//        cpvTest("안녕하세요");
        edtTest = findViewById(R.id.edt_test);
        txtResult = (TextView) findViewById(R.id.txt_result);
        btnStart = (Button) findViewById(R.id.btn_start);
        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, CLIENT_ID);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
                    mResult = "";
                    txtResult.setText("Connecting...");
                    btnStart.setText(R.string.str_stop);
                    naverRecognizer.recognize();
                } else {
                    Log.d(TAG, "stop and wait Final Result");
                    btnStart.setEnabled(false);
                    naverRecognizer.getSpeechRecognizer().stop();
                }
            }
        });
    }

    public void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        naverRecognizer.getSpeechRecognizer().initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResult = "";
        txtResult.setText("");
        btnStart.setText(R.string.str_start);
        btnStart.setEnabled(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        naverRecognizer.getSpeechRecognizer().release();
    }

    static class RecognitionHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        RecognitionHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    void postTest(String word, ArrayList<String> similarWord){
        showProgressDialog();
        final MainService mainService = new MainService(this);
        mainService.postTest(word, similarWord);
    }

    @Override
    public void validateSuccess(String text) {
        hideProgressDialog();
        showCustomToast("성공");
//        mTvHelloWorld.setText(text);
    }

    @Override
    public void validateFailure(@Nullable String message) {
        hideProgressDialog();
//        Looper.prepare();
        showCustomToast(message == null || message.isEmpty() ? getString(R.string.network_error) : message);
//        Looper.loop();
    }

    @Override
    public void cpvSuccess(InputStream is) {
        hideProgressDialog();
        int read = 0;
        byte[] bytes = new byte[1024];
        // 랜덤한 이름으로 mp3 파일 생성
        String tempname = Long.valueOf(new Date().getTime()).toString();
        File f = new File(getFilesDir(), tempname + ".mp3");
        System.out.println("file path: " + f.getAbsolutePath());
        try {
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
            is.close();
            System.out.println("성공6");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    void cpvTest(final String input){
        showProgressDialog();
        final MainService mainService = new MainService(this);
         new Thread(){
             @Override
             public void run() {
                 mainService.cpv(input);
             }
         }.start();
    }

//    public void customOnClick(View view) {
//        switch (view.getId()) {
//            case R.id.main_btn_hello_world:
//                new Thread(){
//                    @Override
//                    public void run() {
//                        testApi();
//                    }
//                }.start();
////                tryGetTest();
//                break;
//            default:
//                break;
//        }
//    }

//    void textToSpeech(String input){
//        String clientId = "g0fd605ajk";
//        String clientSecret = "ZgiGkHGhY3kNc5ulmYD70rkKAM3FeGnONBZpjN63";
//        try {
//            String text = URLEncoder.encode(input, "UTF-8"); // 13자
//            String apiURL = "https://naveropenapi.apigw.ntruss.com/voice-premium/v1/tts";
//            URL url = new URL(apiURL);
//            HttpURLConnection con = (HttpURLConnection)url.openConnection();
//            con.setRequestMethod("POST");
//            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
//            con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
//            // post request
//            String postParams = "speaker=nara&volume=0&speed=0&pitch=0&emotion=0&format=mp3&text=" + text;
//            con.setDoOutput(true);
//            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//            wr.writeBytes(postParams);
//            wr.flush();
//            wr.close();
//            int responseCode = con.getResponseCode();
//            BufferedReader br;
//            if(responseCode==200) { // 정상 호출
//                System.out.println("성공");
//                InputStream is = con.getInputStream();
//                System.out.println("성공2");
//                int read = 0;
//                byte[] bytes = new byte[1024];
//                // 랜덤한 이름으로 mp3 파일 생성
//                String tempname = Long.valueOf(new Date().getTime()).toString();
//                File f = new File(getFilesDir(), tempname + ".mp3");
//                System.out.println("file path: " + f.getAbsolutePath());
//                f.createNewFile();
//                System.out.println("성공3");
//                OutputStream outputStream = new FileOutputStream(f);
//                System.out.println("성공4");
//                while ((read =is.read(bytes)) != -1) {
//                    outputStream.write(bytes, 0, read);
//                }
//                System.out.println("성공5");
//                mediaPlayer = new MediaPlayer();
//                mediaPlayer.setDataSource(f.getAbsolutePath());
//                mediaPlayer.prepare();
//                mediaPlayer.start();
//                is.close();
//                System.out.println("성공6");
//            } else {  // 오류 발생
//                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
//                String inputLine;
//                StringBuffer response = new StringBuffer();
//                while ((inputLine = br.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                br.close();
//                System.out.println("리스폰스 에러: " + response.toString());
//            }
//        } catch (Exception e) {
//            System.out.println("error: " + e);
//        }
//    }

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
    protected void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
    }
}
