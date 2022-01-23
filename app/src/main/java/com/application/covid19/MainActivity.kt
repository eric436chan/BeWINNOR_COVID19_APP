package com.application.covid19

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.*
import androidx.cardview.widget.CardView
import com.yabu.livechart.view.LiveChart
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    //references to GUI components
    private lateinit var questionnaireCardView: CardView

    private lateinit var statesSpinner: Spinner

    private lateinit var infectedLinearLayout: LinearLayout
    private lateinit var vaccinatedLinearLayout: LinearLayout
    private lateinit var deathsLinearLayout: LinearLayout

    private lateinit var dateTextView: TextView
    private lateinit var settingsImageView: ImageView

    private lateinit var latestUpdateTitleTextView: TextView
    private lateinit var latestUpdateTextView: TextView

    private lateinit var infectedTextView: TextView
    private lateinit var vaccinatedTextView: TextView
    private lateinit var deathsTextView: TextView

    private lateinit var newInfectedTextView: TextView
    private lateinit var newVaccinatedTextView: TextView
    private lateinit var newDeathsTextView: TextView

    private lateinit var detailsLinkTextView: TextView

    private lateinit var lineChartTextView: TextView

    private lateinit var lineChart: LiveChart

    private lateinit var lineChartDateTextView: TextView
    private lateinit var lineChartDailyTextView: TextView
    private lateinit var lineChartSumTextView: TextView

    private lateinit var choroplethTextView: TextView

    private lateinit var d3WebView: WebView

    private lateinit var d3StateTextView: TextView
    private lateinit var d3TotalTextView: TextView

    private var visSelected = "INFECTED"
    private var stateSelected = "US"
    private var codeSelected = "US"

    override fun onCreate(savedInstanceState: Bundle?) {

        //Show layout
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_white)
        setContentView(R.layout.activity_main)

        //Get GUI Components
        questionnaireCardView = findViewById(R.id.cv_questionnaire_button)

        statesSpinner = findViewById(R.id.sp_states)

        infectedLinearLayout = findViewById(R.id.cv_infected)
        vaccinatedLinearLayout = findViewById(R.id.cv_vaccinated)
        deathsLinearLayout = findViewById(R.id.cv_deaths)

        dateTextView = findViewById(R.id.tv_date)
        settingsImageView = findViewById(R.id.iv_settings)

        latestUpdateTitleTextView = findViewById(R.id.tv_latest_update_title)
        latestUpdateTextView = findViewById(R.id.tv_latest_update)

        infectedTextView = findViewById(R.id.tv_infected)
        vaccinatedTextView = findViewById(R.id.tv_vaccinated)
        deathsTextView = findViewById(R.id.tv_deaths)

        newInfectedTextView = findViewById(R.id.tv_new_infected)
        newVaccinatedTextView = findViewById(R.id.tv_new_vaccinated)
        newDeathsTextView = findViewById(R.id.tv_new_deaths)

        detailsLinkTextView = findViewById(R.id.tv_details)

        lineChartTextView = findViewById(R.id.tv_line_chart)

        lineChart = findViewById(R.id.line_chart)
        lineChartDateTextView = findViewById(R.id.live_chart_date)
        lineChartSumTextView = findViewById(R.id.live_chart_sum)
        lineChartDailyTextView = findViewById(R.id.live_chart_daily)

        d3WebView = findViewById(R.id.wv_d3)

        choroplethTextView = findViewById(R.id.tv_choropleth)

        d3StateTextView = findViewById(R.id.tv_d3_state)
        d3TotalTextView = findViewById(R.id.tv_d3_total)

        // Assign WebView settings
        d3WebView.settings.javaScriptEnabled = true
        d3WebView.settings.useWideViewPort = true
        d3WebView.settings.allowContentAccess = true
        d3WebView.settings.allowFileAccess = true
        d3WebView.setInitialScale(1)

        //SHARED PREFERENCES TO BE ADDED LATER


        dateTextView.text = SimpleDateFormat("EEEE , MMM d, yyyy", Locale.US).format(Calendar.getInstance().time)


        // Assign items and styling to spinner
        val statesSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this, R.array.states, R.layout.spinner_states_item)
        statesSpinnerAdapter.setDropDownViewResource(R.layout.spinner_states_dropdown_item)
        statesSpinner.adapter = statesSpinnerAdapter


        //STATES SPINNER LISTENER TO BE ADDED LATER

        //CARD LISTENERS TO BE ADDED LATER

        //WEBVIEW LISTENERS TO BE ADDED LATER



        //Settings link listener
        settingsImageView.setOnClickListener{
            //Starts settings activity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        //Questionnaire link listener
        questionnaireCardView.setOnClickListener {
            //Starts questionnaire activity
            val intent = Intent(this, QuestionnaireActivity::class.java)
            startActivity(intent)
        }


        //CODE ADDED LATER




    }
}