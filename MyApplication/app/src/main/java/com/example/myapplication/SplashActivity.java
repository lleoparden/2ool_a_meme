package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        VideoView videoView = findViewById(R.id.videoView);

        // Set the video path (from raw folder)
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.splash_video;
        videoView.setVideoURI(Uri.parse(videoPath));

        // Start the video
        videoView.start();

        // Set a listener to navigate to the next activity when the video finishes
        videoView.setOnCompletionListener(mp -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the splash activity
        });
    }
}