package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

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

        // Check authentication when video finishes
        videoView.setOnCompletionListener(mp -> {
            checkAuthState();
        });
    }

    private void checkAuthState() {
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is already signed in, go to MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            // No user is signed in, go to onboarding
            Intent intent = new Intent(SplashActivity.this, onboardingActivity.class);
            startActivity(intent);
        }

        finish(); // Close the splash activity
    }
}