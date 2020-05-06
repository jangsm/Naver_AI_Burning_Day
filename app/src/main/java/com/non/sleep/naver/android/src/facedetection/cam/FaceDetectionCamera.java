package com.non.sleep.naver.android.src.facedetection.cam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.non.sleep.naver.android.src.facedetection.FaceDetectionActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Manages the android camera and sets it up for face detection
 * can throw an error if face detection is not supported on this device
 */
public class FaceDetectionCamera implements OneShotFaceDetectionListener.Listener {

    private final Camera camera;

    private Listener listener;



    public FaceDetectionCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Use this to detect faces when you have a custom surface to display upon
     *
     * @param listener the {@link com.non.sleep.naver.android.src.facedetection.cam.FaceDetectionCamera.Listener} for when faces are detected
     * @param holder   the {@link android.view.SurfaceHolder} to display upon
     */
    public void initialise(Listener listener, SurfaceHolder holder) {
        this.listener = listener;
        try {
            camera.stopPreview();
        } catch (Exception swallow) {
            // ignore: tried to stop a non-existent preview
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
            camera.setPreviewCallback(previewCallback);
            camera.setFaceDetectionListener(new OneShotFaceDetectionListener(this));
            camera.startFaceDetection();
        } catch (IOException e) {
            this.listener.onFaceDetectionNonRecoverableError();
        }
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if(FaceDetectionActivity.isCapture == true) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Camera.Parameters parameters = camera.getParameters();
                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;
                ByteArrayOutputStream outstr = new ByteArrayOutputStream();
                Rect rect = new Rect(0, 0, width, height);
                YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
                yuvimage.compressToJpeg(rect, 100, outstr);
                Bitmap bmp = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());

                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                String tempname = Long.valueOf(new Date().getTime()).toString();
                saveImage(rotatedBitmap, tempname);
                FaceDetectionActivity.isCapture = false;
                Log.i("VSDVDS", "SVDSDV");
//                Toast.makeText(FaceDetectionActivity.context, "CAPTURE", Toast.LENGTH_SHORT).show();
            }
        }
    };
    // face detect screen -> get byte array -> bitmap -> jpg

    private void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".jpg";
        final File file = new File(myDir, fname);
        System.out.println("path: " + file.getAbsolutePath());

        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(){
            @Override
            public void run() {
                listener.textToSpeech();
            }
        }.start();

        try {
            Thread.sleep(1500);
        } catch (Exception e){
            e.printStackTrace();
        }
        new Thread(){
            @Override
            public void run() {
                testApi(file.getAbsolutePath());
            }
        }.start();
    }
    // save bitmap -> image



    void testApi(String path){
//        StringBuffer reqStr = new StringBuffer();
        System.out.println("실행?");
        String clientId = "g0fd605ajk";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "ZgiGkHGhY3kNc5ulmYD70rkKAM3FeGnONBZpjN63";//애플리케이션 클라이언트 시크릿값";

        try {
            String paramName = "image"; // 파라미터명은 image로 지정
            String imgFile = path;
            File uploadFile = new File(imgFile);
            String apiURL = "https://naveropenapi.apigw.ntruss.com/vision/v1/face"; // 얼굴 감지
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);
            // multipart request
            String boundary = "---" + System.currentTimeMillis() + "---";
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
            OutputStream outputStream = con.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            String LINE_FEED = "\r\n";
            // file 추가
            String fileName = uploadFile.getName();
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
            writer.append("Content-Type: "  + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();
            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();
            BufferedReader br = null;
            int responseCode = con.getResponseCode();
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 오류 발생
                System.out.println("error!!!!!!! responseCode= " + responseCode);
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }
            String inputLine;
            if(br != null) {
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println("리스폰스: " + response.toString());
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("faces");
                if(jsonArray.length()>0){
                    System.out.println("성별: " + jsonArray.getJSONObject(0).getJSONObject("gender").getString("value"));
                    System.out.println("연령대: " + jsonArray.getJSONObject(0).getJSONObject("age").getString("value"));
                    String gender = null;
                    if (jsonArray.getJSONObject(0).getJSONObject("gender").getString("value").equals("female")){
                        gender = "F";
                    }
                    else if(jsonArray.getJSONObject(0).getJSONObject("gender").getString("value").equals("male")){
                        gender = "M";
                    }
                    else{
                        gender = "C";
                    }
                    String age = jsonArray.getJSONObject(0).getJSONObject("age").getString("value");
                    age = age.substring(0,age.indexOf('~'));
                    int ageInt = Integer.parseInt(age);
                    ageInt = ageInt-ageInt%10;
                    System.out.println("나이 인트값: " + ageInt);
                    System.out.println("기분: " + jsonArray.getJSONObject(0).getJSONObject("emotion").getString("value"));
                    listener.nextActivity(ageInt, gender);
                }
                else{
                    listener.otherNextActivity();
                }

                // 서버로 성별, 연령대, 기분을 보내주면 리스폰스로 추천메뉴를 받고
//                listener.nextActivity();
            } else {
                System.out.println("error !!!");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }



    @Override
    public void onFaceDetected() {
        listener.onFaceDetected();
    }

    @Override
    public void onFaceTimedOut() {
        listener.onFaceTimedOut();
    }

    public void recycle() {
        if (camera != null) {
            camera.release();
        }
    }

    public interface Listener {
        void onFaceDetected();

        void onFaceTimedOut();

        void onFaceDetectionNonRecoverableError();

        void nextActivity(int age, String gender);

        void otherNextActivity();

        void textToSpeech();
    }


}