package com.application.covid19

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.HtmlCompat

class SettingsActivity : AppCompatActivity() {

    //references to GUI components
    private lateinit var mainActivityButton: ImageView

    private lateinit var dataDetailsTextView1: TextView
    private lateinit var dataDetailsTextView2: TextView
    private lateinit var dataDetailsTextView3: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        //Show layout
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_white)
        setContentView(R.layout.activity_settings)

        //Get GUI Components
        mainActivityButton = findViewById(R.id.back_from_settings)
        dataDetailsTextView1 = findViewById(R.id.the_data_description1)
        dataDetailsTextView2 = findViewById(R.id.the_data_description2)
        dataDetailsTextView3 = findViewById(R.id.the_data_description3)

        mainActivityButton.setOnClickListener {
            onBackPressed()
        }

        // Remove title bar
        if (supportActionBar != null)
            supportActionBar?.hide()

        // Suppress Dark Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Links data description
        spanUrl(
            HtmlCompat.fromHtml(
                "<a href='https://covid19api.com/'>COVID19API</a>, a free API for data on the Coronavirus sourced from <a href='https://github.com/CSSEGISandData/COVID-19'>Johns Hopkins CSSE.</a>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ) as Spannable, dataDetailsTextView1
        )

        spanUrl(
            HtmlCompat.fromHtml(
                "<a href='https://github.com/nytimes/covid-19-data'>The New York Times COVID-19 Data</a>, a collection of COVID-19 Data gathered by The New York Times.",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ) as Spannable, dataDetailsTextView2
        )

        spanUrl(
            HtmlCompat.fromHtml(
                "<a href='https://data.cdc.gov/'>Data.CDC.gov</a>, a collection of COVID-19 Vaccinations numbers for <a href='https://data.cdc.gov/Vaccinations/COVID-19-Vaccine-Distribution-Allocations-by-Juris/saz5-9hgg'>Pfizer</a>, <a href='https://data.cdc.gov/Vaccinations/COVID-19-Vaccine-Distribution-Allocations-by-Juris/b7pe-5nws'>Moderna</a>, and <a href='https://data.cdc.gov/Vaccinations/COVID-19-Vaccine-Distribution-Allocations-by-Juris/w9zu-fywh'>Janssen</a> vaccinations.",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ) as Spannable, dataDetailsTextView3
        )
    }

    private fun spanUrl(spannableLink: Spannable, text: TextView) {
        for (u in spannableLink.getSpans(0, spannableLink.length, URLSpan::class.java)) {
            spannableLink.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }, spannableLink.getSpanStart(u), spannableLink.getSpanEnd(u), 0)
        }

        text.text = spannableLink
        text.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        this.finish()
    }
}