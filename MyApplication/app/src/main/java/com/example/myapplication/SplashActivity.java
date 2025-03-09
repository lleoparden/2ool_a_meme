package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        // Hide navigation and status bars (Full-Screen Mode)
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        VideoView videoView = findViewById(R.id.videoView);

        // Set the video path (from raw folder)
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.splash_video;
        videoView.setVideoURI(Uri.parse(videoPath));

        // Start the video only when it is prepared to prevent ANR
        videoView.setOnPreparedListener(mp -> videoView.start());

        // Navigate to OnboardingActivity when video finishes
        videoView.setOnCompletionListener(mp -> {
            Intent intent = new Intent(SplashActivity.this, onboardingActivity.class);
            startActivity(intent);
            finish(); // Close the splash activity
        });
    }
}
