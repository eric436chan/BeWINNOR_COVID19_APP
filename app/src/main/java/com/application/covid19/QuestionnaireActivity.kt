package com.application.covid19

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate

class QuestionnaireActivity : AppCompatActivity() {

    //Reference to GUI Component
    private lateinit var mainActivityButton: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {

        //Show layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)

        // Get layout components
        mainActivityButton = findViewById(R.id.btn_back_questionnaire)


        // Main activity button listener
        mainActivityButton.setOnClickListener {
            onBackPressed()
        }

//        // Remove title bar
//        if (supportActionBar != null)
//            supportActionBar?.hide()
//
//        // Suppress dark mode
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        this.finish()
    }
}