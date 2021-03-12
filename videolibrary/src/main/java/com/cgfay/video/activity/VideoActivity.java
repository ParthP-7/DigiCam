package com.cgfay.video.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.cgfay.video.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {

    VideoView vid;
    String videopath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent intent = getIntent();
        videopath = intent.getExtras().getString("vidspath");

        vid = (VideoView)findViewById(R.id.videoView);
    }
    public void playVideo(View v) {
        MediaController m = new MediaController(this);
        vid.setMediaController(m);

        Uri u = Uri.parse(videopath);

        vid.setVideoURI(u);

        vid.start();

    }
}