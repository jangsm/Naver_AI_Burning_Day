package com.non.sleep.naver.android.src.facedetection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.non.sleep.naver.android.R;
import com.non.sleep.naver.android.src.facedetection.cam.FaceDetectionCamera;
import com.non.sleep.naver.android.src.facedetection.cam.FrontCameraRetriever;
import com.non.sleep.naver.android.src.menu_list.MenuListActivitiy;
import com.non.sleep.naver.android.src.recommend.RecommendActivity;

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
import java.util.Date;

/**
 * Don't forget to add the permissions to the AndroidManifest.xml!
 * <p/>
 * <uses-feature android:name="android.hardware.camera" />
 * <uses-feature android:name="android.hardware.camera.front" />
 * <p/>
 * <uses-permission android:name="android.permission.CAMERA" />
 */
public class FaceDetectionActivity extends Activity implements FrontCameraRetriever.Listener, FaceDetectionCamera.Listener {

    private static final String TAG = "FDT" + FaceDetectionActivity.class.getSimpleName();
    public static ImageView iv;
    private TextView helloWorldTextView;

    public static FrameLayout layout;
    public static Context context;
    private SurfaceView cameraSurface;
    public static Bitmap shareBitmap;
    public static boolean isCapture;
    public static int count=0;
    private TextView tv;

    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facedetection);
        isCapture = false;
        // Go get the front facing camera of the device
        // best practice is to do this asynchronously

        permissionCheck();

        context = this;
        layout = (FrameLayout) findViewById(R.id.helloWorldCameraPreview);
        FrontCameraRetriever.retrieveFor(this);
        count=0;
    }

    public void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onLoaded(FaceDetectionCamera camera) {
        // When the front facing camera has been retrieved we still need to ensure our display is ready
        // so we will let the camera surface view initialise the camera i.e turn face detection on
        cameraSurface = new CameraSurfaceView(this, camera, this);
        // Add the surface view (i.e. camera preview to our layout)
        ((FrameLayout) findViewById(R.id.helloWorldCameraPreview)).addView(cameraSurface);
    }

    @Override
    public void onFailedToLoadFaceDetectionCamera() {
        // This can happen if
        // there is no front facing camera
        // or another app is using the camera
        // or our app or another app failed to release the camera properly
        Log.wtf(TAG, "Failed to load camera, what went wrong?");
//        helloWorldTextView.setText(R.string.error_with_face_detection);
    }

    @Override
    public void onFaceDetected() {
//        helloWorldTextView.setText(R.string.face_detected_message);
        View v1 = FaceDetectionActivity.layout.getRootView();
        v1.buildDrawingCache();
        v1.setDrawingCacheEnabled(true);

        isCapture = true;
        // set face detect

//        Toast.makeText(context, "DETECTED", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onFaceTimedOut() {
//        helloWorldTextView.setText(R.string.face_detected_then_lost_message);
//        Toast.makeText(context, "TIMEOUT", Toast.LENGTH_SHORT).show();
        isCapture = false;
    }

    @Override
    public void onFaceDetectionNonRecoverableError() {
        // This can happen if
        // Face detection not supported on this device
        // Something went wrong in the Android api
        // or our app or another app failed to release the camera properly
//        helloWorldTextView.setText(R.string.error_with_face_detection);
//        Toast.makeText(context, "NONRECOVER", Toast.LENGTH_SHORT).show();
        isCapture = false;

    }

    @Override
    public void nextActivity(int age, String gender) {
        Intent intent = new Intent(FaceDetectionActivity.this, RecommendActivity.class);
        intent.putExtra("age",age);
        intent.putExtra("gender",gender);
        startActivity(intent);
        finish();
    }

    @Override
    public void otherNextActivity() {
        startActivity(new Intent(FaceDetectionActivity.this, MenuListActivitiy.class));
        finish();
    }

    @Override
    public void textToSpeech() {
        String clientId = "g0fd605ajk";
        String clientSecret = "ZgiGkHGhY3kNc5ulmYD70rkKAM3FeGnONBZpjN63";
        try {
            String text = URLEncoder.encode("안녕하세요.", "UTF-8"); // 13자
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
                is.close();
                System.out.println("성공6");
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
    protected void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
    }
}