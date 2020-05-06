package com.non.sleep.naver.android.src.start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.non.sleep.naver.android.R;
import com.non.sleep.naver.android.src.recommend.RecommendActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ImageView mImageViewStart = findViewById(R.id.start_iv_speak);
        mImageViewStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, RecommendActivity.class));
            }
        });
    }


}
