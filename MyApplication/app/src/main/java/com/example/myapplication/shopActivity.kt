package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class shopActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var shopButton: ImageButton
    private lateinit var explainationButton: ImageButton
    private lateinit var gamesButton: ImageButton


    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    // User data
    private lateinit var currentUsername: String
    private lateinit var currentUserId: String
    private lateinit var accountButton: ImageButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.shop)

        shopButton = findViewById(R.id.shop)
        explainationButton = findViewById(R.id.shar7)
        gamesButton = findViewById(R.id.games)
        homeButton = findViewById(R.id.homebutton)
        accountButton = findViewById(R.id.account)

        shopButton.setOnClickListener {
            val intent = Intent(this, shopActivity::class.java)
            startActivity(intent)
        }

        explainationButton.setOnClickListener {
            val intent = Intent(this, explainationActivity::class.java)
            startActivity(intent)
        }


        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        gamesButton.setOnClickListener {
            val intent = Intent(this, gamesActivity::class.java)
            startActivity(intent)
        }

        database = FirebaseDatabase.getInstance()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        currentUserId = auth.currentUser!!.uid
        val userRef = database.getReference("users").child(currentUserId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentUsername = snapshot.child("name").getValue(String::class.java) ?: "Anonymous"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@shopActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })

        currentUserId = auth.currentUser!!.uid
        loadUserData(currentUserId)
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