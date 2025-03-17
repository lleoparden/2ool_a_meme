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
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {

    private lateinit var signOutButton: Button
    private lateinit var sidebar_header: TextView
    private lateinit var sidebarButton: ImageButton
    private lateinit var dimmingOverlay: View
    private lateinit var sidebarContainer: ConstraintLayout
    private lateinit var chatButton: ImageButton
    private lateinit var shopButton1: ImageButton
    private lateinit var shopButton2: ImageButton
    private lateinit var explainationButton1: ImageButton
    private lateinit var explainationButton2: ImageButton
    private lateinit var gamesButton1: ImageButton
    private lateinit var gamesButton2: ImageButton
    private lateinit var accountButton: ImageButton
    private var isSidebarOpen = false


    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    // User data
    private lateinit var currentUsername: String
    private lateinit var currentUserId: String





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
        chatButton = findViewById(R.id.livechat)
        shopButton1 = findViewById(R.id.shop)
        explainationButton1 = findViewById(R.id.shar7)
        gamesButton1 = findViewById(R.id.games)
        explainationButton2 = findViewById(R.id.shar7_big)
        gamesButton2 = findViewById(R.id.games_big)
        shopButton2 = findViewById(R.id.merch)
        accountButton = findViewById(R.id.account)

        chatButton.setOnClickListener {
            val intent = Intent(this, chatRoomActivity::class.java)
            startActivity(intent)
            finish()
        }

        shopButton1.setOnClickListener {
            val intent = Intent(this, shopActivity::class.java)
            startActivity(intent)
            finish()
        }

        shopButton2.setOnClickListener {
            val intent = Intent(this, shopActivity::class.java)
            startActivity(intent)
            finish()
        }

        explainationButton1.setOnClickListener {
            val intent = Intent(this, explainationActivity::class.java)
            startActivity(intent)
            finish()
        }

        explainationButton2.setOnClickListener {
            val intent = Intent(this, explainationActivity::class.java)
            startActivity(intent)
            finish()
        }

        gamesButton1.setOnClickListener {
            val intent = Intent(this, gamesActivity::class.java)
            startActivity(intent)
            finish()
        }

        gamesButton2.setOnClickListener {
            val intent = Intent(this, gamesActivity::class.java)
            startActivity(intent)
            finish()
        }

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


        database = FirebaseDatabase.getInstance()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        sidebar_header.text = "Welcome, " + user?.displayName ?: "Guest"

        currentUserId = auth.currentUser!!.uid
        val userRef = database.getReference("users").child(currentUserId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentUsername = snapshot.child("name").getValue(String::class.java) ?: "Anonymous"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })

        currentUserId = auth.currentUser!!.uid
        loadUserData(currentUserId)



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
            val intent = Intent(this@MainActivity, onBoardingActivity::class.java)
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
    private fun loadUserData(userId: String?) {
        userId?.let {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(it)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val pfpIndex = snapshot.child("pfp").getValue(Int::class.java) ?: 0
                        updateProfilePicture(pfpIndex)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, "Error loading user data", error.toException())
                }
            })
        }
    }

    private fun updateProfilePicture(pfpIndex: Int) {

        val pfps = arrayOf(
            R.drawable.pfp1,
            R.drawable.pfp2,
            R.drawable.pfp3,
            R.drawable.pfp4,
            R.drawable.pfp5,
            R.drawable.pfp6,
        )

        val index = (pfpIndex - 1).coerceIn(0, pfps.size - 1)
        accountButton.setImageResource(pfps[index])
    }


}
