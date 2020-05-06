package com.non.sleep.naver.android.src.facedetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.non.sleep.naver.android.src.facedetection.cam.FaceDetectionCamera;

import java.io.ByteArrayOutputStream;

@SuppressLint("ViewConstructor") // View can only be inflated programatically
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private final FaceDetectionCamera camera;
    private final FaceDetectionCamera.Listener listener;

    public CameraSurfaceView(Context context, FaceDetectionCamera camera, FaceDetectionCamera.Listener listener) {
        super(context);
        this.camera = camera;
        this.listener = listener;
        // Listen for when the surface is ready to be drawn on
        this.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // When the surface is ready to be drawn on
        // tell our camera to use this to show a previewI
        camera.initialise(listener, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceDoesNotExist()) {
            return;
        }
        // When the surface changes we need to re-attach it to our camera
        camera.initialise(listener, holder);
    }

    private boolean surfaceDoesNotExist() {
        return getHolder().getSurface() == null;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        // (done in FrontCameraRetriever for us)
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Camera.Parameters params = camera.getParameters();
        int w = params.getPreviewSize().width;
        int h = params.getPreviewSize().height;
        int format = params.getPreviewFormat();
        YuvImage image = new YuvImage(data, format, w, h, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rect area = new Rect(0, 0, w, h);
        image.compressToJpeg(area, 100, out);
        Bitmap bm = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());

        Matrix matrix = new Matrix();
        matrix.postRotate(0);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0,w, h, matrix, true);
        FaceDetectionActivity.shareBitmap=rotatedBitmap;
    }
}