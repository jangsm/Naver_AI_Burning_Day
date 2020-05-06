package com.non.sleep.naver.android.src.firebaseFaceDetection;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.non.sleep.naver.android.R;

public class FireBaseFaceDetectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // High-accuracy landmark detection and face classification
        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

// Real-time contour detection of multiple faces
        FirebaseVisionFaceDetectorOptions realTimeOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();

    }


//    private class YourAnalyzer implements ImageAnalysis.Analyzer {
//
//        private int degreesToFirebaseRotation(int degrees) {
//            switch (degrees) {
//                case 0:
//                    return FirebaseVisionImageMetadata.ROTATION_0;
//                case 90:
//                    return FirebaseVisionImageMetadata.ROTATION_90;
//                case 180:
//                    return FirebaseVisionImageMetadata.ROTATION_180;
//                case 270:
//                    return FirebaseVisionImageMetadata.ROTATION_270;
//                default:
//                    throw new IllegalArgumentException(
//                            "Rotation must be 0, 90, 180, or 270.");
//            }
//        }
//
//        @Override
//        public void analyze(ImageProxy imageProxy, int degrees) {
//            if (imageProxy == null || imageProxy.getImage() == null) {
//                return;
//            }
//            Image mediaImage = imageProxy.getImage();
//            int rotation = degreesToFirebaseRotation(degrees);
//            FirebaseVisionImage image =
//                    FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
//            // Pass image to an ML Kit Vision API
//            // ...
//        }
//    }



}
