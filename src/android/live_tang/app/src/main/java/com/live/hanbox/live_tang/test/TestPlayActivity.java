package com.live.hanbox.live_tang.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.live.hanbox.live_tang.R;
import com.live.hanbox.live_tang.view.live_baidu.VideoViewPlayingActivity;

public class TestPlayActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private Button mPlayBtn;
    private EditText mSourceET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_play);

        initUI();
    }

    void initUI(){
        mPlayBtn = (Button)findViewById(R.id.playBtn);
        mSourceET = (EditText)findViewById(R.id.getET);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });
    }

    private void playVideo(){
        String source = mSourceET.getText().toString();
        if(source == null || source.equals("")){
            /**
             * 简单检测播放源的合法性,不合法不播放
             */
            source = "http://play.tangshanren.com/tangshanren/test1.m3u8";
            Intent intent = new Intent(this, VideoViewPlayingActivity.class);
            intent.putExtra("pull_url", source);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, VideoViewPlayingActivity.class);
            intent.putExtra("pull_url", source);
            startActivity(intent);
        }
    }
}
