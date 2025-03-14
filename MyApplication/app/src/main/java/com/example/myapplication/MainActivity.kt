package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    private lateinit var signOutButton: Button
    private lateinit var sidebar_header: TextView
    private lateinit var sidebarButton: ImageButton
    private lateinit var dimmingOverlay: View
    private lateinit var sidebarContainer: ConstraintLayout
    private var isSidebarOpen = false
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mainmenu)


        signOutButton = findViewById(R.id.signoutbutton)
        signOutButton.setOnClickListener {
            signOut()
        }

        // Initialize views
        sidebarButton = findViewById(R.id.sidebar_button)
        dimmingOverlay = findViewById(R.id.dimming_overlay)
        sidebarContainer = findViewById(R.id.sidebar_container)
        sidebar_header = findViewById(R.id.sidebar_header)


        // Set initial state
        sidebarContainer.post {
            sidebarContainer.translationX = -sidebarContainer.width.toFloat()
        }
        dimmingOverlay.visibility = View.GONE



        // Set click listener for sidebar button
        sidebarButton.setOnClickListener {
            toggleSidebar()
        }

        // Set click listener for dimming overlay (to close sidebar when clicking outside)
        dimmingOverlay.setOnClickListener {
            if (isSidebarOpen) {
                toggleSidebar()
            }
        }
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        sidebar_header.text = "Welcome, " + user?.displayName ?: "Guest"


    }

    private fun signOut() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Also sign out from Google if using Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener(this) { task ->
            // After sign out, redirect to onboarding or sign-in screen
            val intent = Intent(this@MainActivity, onboardingActivity::class.java)
            // Clear the back stack so the user can't go back to MainActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun toggleSidebar() {
        if (isSidebarOpen) {
            // Close sidebar
            val sidebarAnimator = ObjectAnimator.ofFloat(
                sidebarContainer,
                "translationX",
                0f,
                -sidebarContainer.width.toFloat()
            )

            val overlayAnimator = ObjectAnimator.ofFloat(
                dimmingOverlay,
                "alpha",
                0.7f,
                0f
            )

            AnimatorSet().apply {
                playTogether(sidebarAnimator, overlayAnimator)
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        dimmingOverlay.visibility = View.GONE
                    }
                })
                start()
            }
        } else {
            // Open sidebar
            dimmingOverlay.visibility = View.VISIBLE
            dimmingOverlay.alpha = 0f

            val sidebarAnimator = ObjectAnimator.ofFloat(
                sidebarContainer,
                "translationX",
                -sidebarContainer.width.toFloat(),
                0f
            )

            val overlayAnimator = ObjectAnimator.ofFloat(
                dimmingOverlay,
                "alpha",
                0f,
                0.7f
            )

            AnimatorSet().apply {
                playTogether(sidebarAnimator, overlayAnimator)
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }

        isSidebarOpen = !isSidebarOpen
    }

}
