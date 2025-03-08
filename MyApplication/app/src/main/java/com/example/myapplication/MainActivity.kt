package com.example.myapplication

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var gestureDetector: GestureDetector

    private val images = arrayOf(
        R.drawable.onboarding1,
        R.drawable.onboarding2,
        R.drawable.onboarding3
    )
    private var currentIndex = 0
    private var isImage1Visible = true // Track which image view is currently showing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.onboarding)

        imageView1 = findViewById(R.id.imageButton1)
        imageView2 = findViewById(R.id.imageButton2)

        imageView1.setImageResource(images[currentIndex])
        imageView2.visibility = View.INVISIBLE // Hide second image initially

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Initialize Gesture Detector
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null && e2 != null) {
                    val diffX = e2.x - e1.x
                    if (diffX > 100) { // Swipe Right
                        if (currentIndex != 0 ) {
                            changeImage(1, R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                        return true
                    } else if (diffX < -100) { // Swipe Left
                        if (currentIndex != 1) {
                            changeImage(-1, R.anim.slide_in_left, R.anim.slide_out_right)
                        }else{
                            setContentView(R.layout.sign_in)
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!) || super.onTouchEvent(event)
    }

    private fun changeImage(direction: Int, animIn: Int, animOut: Int) {
        val fadeOut = AnimationUtils.loadAnimation(this, animOut)
        val fadeIn = AnimationUtils.loadAnimation(this, animIn)

        // Select which image view is currently visible
        val currentImageView = if (isImage1Visible) imageView1 else imageView2
        val nextImageView = if (isImage1Visible) imageView2 else imageView1

        // Calculate the next index correctly
        currentIndex = (currentIndex + direction + images.size) % images.size
        nextImageView.setImageResource(images[currentIndex])
        nextImageView.visibility = View.VISIBLE

        // Play animations
        currentImageView.startAnimation(fadeOut)
        nextImageView.startAnimation(fadeIn)

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                currentImageView.visibility = View.INVISIBLE
                isImage1Visible = !isImage1Visible
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}
