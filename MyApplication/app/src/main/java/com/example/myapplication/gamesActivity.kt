package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class gamesActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var shopButton: ImageButton
    private lateinit var explainationButton: ImageButton
    private lateinit var livechat: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.games)


        shopButton = findViewById(R.id.shop)
        explainationButton = findViewById(R.id.shar7)
        livechat = findViewById(R.id.livechat)
        homeButton = findViewById(R.id.homebutton)

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

        livechat.setOnClickListener {
            val intent = Intent(this, chatRoomActivity::class.java)
            startActivity(intent)
        }




    }
}