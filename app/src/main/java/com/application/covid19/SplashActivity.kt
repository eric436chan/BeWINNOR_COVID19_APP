package com.application.covid19

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        if(supportActionBar != null){
            supportActionBar?.hide()
        }

        //ADD CODE HERE LATER


        // Suppress dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //This makes us go to MainActivity class
        startActivity(Intent(this, MainActivity::class.java))
        this.finish()


        //ADD CODE HERE LATER

    }
}