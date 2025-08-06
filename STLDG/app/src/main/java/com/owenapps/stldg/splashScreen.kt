package com.owenapps.stldg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class splashScreen : AppCompatActivity() {

    private lateinit var starButton: ImageButton
    private lateinit var bgImage: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_bg)
        starButton = findViewById(R.id.imageButtonStar)
        bgImage = findViewById(R.id.backgroundImage)
        Toast.makeText(this, "Loading Word Apps....", Toast.LENGTH_LONG).show()

        starButton.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }

        bgImage.setOnClickListener {
            Toast.makeText(this, "Initializing Word. Please Wait...", Toast.LENGTH_LONG).show()
        }
    }
}