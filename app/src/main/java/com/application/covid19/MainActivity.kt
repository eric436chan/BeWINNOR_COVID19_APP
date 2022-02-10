package com.application.covid19

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.application.covid19.data.*
import com.application.covid19.states.States
import com.yabu.livechart.model.DataPoint
import com.yabu.livechart.model.Dataset
import com.yabu.livechart.view.LiveChart
import com.yabu.livechart.view.LiveChartStyle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    // Initialize global variables
    // These are references to display elements that we'll need for the MainActivity
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

    /**
     * It does a lot of stuff, read comments.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Show layout
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_white)
        setContentView(R.layout.activity_main)

        // Get components from layout
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

        // Get phone's SharePreferences
        val sharedPreferences = getSharedPreferences("COVID_19", Context.MODE_PRIVATE)

        // Set today's date text
        // Date might be different due to errors
        dateTextView.text = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US).format(Calendar.getInstance().time)

        // Assign items and styling to spinner
        val statesSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this, R.array.states, R.layout.spinner_states_item)
        statesSpinnerAdapter.setDropDownViewResource(R.layout.spinner_states_dropdown_item)
        statesSpinner.adapter = statesSpinnerAdapter

        // States spinner listener
        statesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Update global variable stateSelected
                stateSelected = adapterView?.getItemAtPosition(position).toString()

                // Update global variable codeSelected
                codeSelected = if (stateSelected != "All States") {
                    States().getStatesMap()[adapterView?.getItemAtPosition(position)].toString()
                } else {
                    "US"
                }

                // Update data and visualizations
                updateDataAndVisualizations(sharedPreferences, false)

                // Clear Choropleth Map state outline
                d3WebView.pivotX = 0F
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Ignore, there is always an item selected
            }
        }

        // Infected, Vaccinated, and Deaths card listener
        // Updates the global variable visSelected and updates data visualizations
        infectedLinearLayout.setOnClickListener {
            visSelected = "INFECTED"
            updateDataAndVisualizations(sharedPreferences, true)
        }
        vaccinatedLinearLayout.setOnClickListener {
            visSelected = "VACCINATED"
            updateDataAndVisualizations(sharedPreferences, true)
        }
        deathsLinearLayout.setOnClickListener {
            visSelected = "DEATHS"
            updateDataAndVisualizations(sharedPreferences, true)
        }

        // WebView listener
        d3WebView.addJavascriptInterface( object : Any() {
            @JavascriptInterface
            fun setStateText(state: String) {
                runOnUiThread {
                    // Format selected state from "New York" to "NEWYORK" or "All States" to "US"
                    val selectedFormatted = state.replace(" ", "").uppercase(Locale.US)

                    // Gets the state selected and totals
                    val stateText = "State: $state"
                    val totalText =  "Total ${visSelected.lowercase(Locale.US)}: ${sharedPreferences.getString("${selectedFormatted}_${visSelected}", "0")}"

                    // Sets the state selected and totals
                    d3StateTextView.text = stateText
                    d3TotalTextView.text = totalText

                    // Sets spinner value to match map selection
                    var index = 0
                    for (i in 0 until statesSpinner.count) {
                        if (statesSpinner.getItemAtPosition(i).toString() == state) {
                            index = i
                        }
                    }
                    statesSpinner.setSelection(index)
                }
            }

            @JavascriptInterface
            fun setCountryText() {
                runOnUiThread {
                    // Gets the state selected and totals
                    val stateText = "State: All States"
                    val totalText =  "Total ${visSelected.lowercase(Locale.US)}: ${sharedPreferences.getString("US_${visSelected}", "0")}"

                    // Sets the state selected and totals
                    d3StateTextView.text = stateText
                    d3TotalTextView.text = totalText

                    // Sets spinner value to first index [All States]
                    statesSpinner.setSelection(0)
                }
            }
        }, "kotlin")

        // Draw Choropleth Map
        setChoroplethMap(d3WebView, sharedPreferences)

        // Settings link listener
        settingsImageView.setOnClickListener {
            // Starts settings activity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Questionnaire link listener
        questionnaireCardView.setOnClickListener {
            // Starts questionnaire activity
            val intent = Intent(this, QuestionnaireActivity::class.java)
            startActivity(intent)
        }

        // Removes hyperlink's underlining
        val s = HtmlCompat.fromHtml("<a href='https://covid.cdc.gov/covid-data-tracker/#datatracker-home'>Details</a>", HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(object : UnderlineSpan() { override fun updateDrawState(tp: TextPaint) { tp.isUnderlineText = false } }, s.getSpanStart(u), s.getSpanEnd(u), 0)
        }
        detailsLinkTextView.text = s
        // Open CDC hyperlink
        detailsLinkTextView.movementMethod = LinkMovementMethod.getInstance()

        // Remove title bar
        if (supportActionBar != null)
            supportActionBar?.hide()

        // Suppress dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun updateDataAndVisualizations(sharedPreferences: SharedPreferences, calledFromCardView: Boolean) {
        // Set card text
        setCardText(sharedPreferences)
        // Set initial LineChart tooltip text
        setInitialLineChartToolTip(sharedPreferences)
        // Draw LineChart
        setLineChart(lineChart, sharedPreferences)
        // Set initial Choropleth tooltip text
        setInitialChoroplethToolTip(sharedPreferences)
        // Draw Choropleth Map
        if (calledFromCardView) {
            setChoroplethMap(d3WebView, sharedPreferences)
        }
    }

    /**
     * Sets the text for various TextViews with COVID-19 data based on what's found on [sharedPreferences].
     */
    private fun setCardText(sharedPreferences: SharedPreferences) {
        // Set Cards subtitle text
        val latestUpdateTileText = "$codeSelected Cases Overview"
        latestUpdateTitleTextView.text = latestUpdateTileText

        // Format selected state from "New York" to "NEWYORK" or "All States" to "US"
        val state = if (stateSelected == "All States") "US" else stateSelected.uppercase(Locale.ROOT)
            .replace(" ", "")

        // Set last updated text
        latestUpdateTextView.text = sharedPreferences.getString("${state}_UPDATED", "Unknown")

        // Set total numbers
        infectedTextView.text = sharedPreferences.getString("${state}_INFECTED", "Unknown")
        vaccinatedTextView.text = sharedPreferences.getString("${state}_VACCINATED", "Unknown")
        deathsTextView.text = sharedPreferences.getString("${state}_DEATHS", "Unknown")

        // Get new numbers
        val newInfected = sharedPreferences.getString("${state}_NEW_INFECTED", "0")
        val newVaccinated = sharedPreferences.getString("${state}_NEW_VACCINATED", "0")
        val newDeaths = sharedPreferences.getString("${state}_NEW_DEATHS", "0")

        // Format new numbers
        val formattedNewInfected = if (newInfected!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newInfected"
        val formattedNewVaccinated = if (newVaccinated!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newVaccinated"
        val formattedNewDeaths = if (newDeaths!!.replace(",", "").toInt() <= 0) "No Changes" else "+ $newDeaths"

        // Set new numbers
        newInfectedTextView.text = formattedNewInfected
        newVaccinatedTextView.text = formattedNewVaccinated
        newDeathsTextView.text = formattedNewDeaths
    }

    /**
     * Sets the text for the LineChart tooltip's TextViews with COVID-19 data based on what's found on [sharedPreferences].
     */
    private fun setInitialLineChartToolTip(sharedPreferences: SharedPreferences) {
        // Set LineChart subtitle text
        val lineChartText = "$codeSelected ${visSelected.lowercase(Locale.ROOT)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} Over Time"
        lineChartTextView.text = lineChartText

        // Format selected state from "New York" to "NEWYORK" or "All States" to "US"
        val state = if (stateSelected == "All States") "US" else stateSelected.uppercase(Locale.ROOT)
            .replace(" ", "")

        // Get list size
        val listSize = sharedPreferences.getInt("${state}_${visSelected}_LIST_SIZE", 0)

        // Get date and format it as MM/DD/YYYY
        val lineChartDateText = if (sharedPreferences.getString("${state}_${visSelected}_LIST_DATE_${listSize - 1}", "No Date")!!.contains("/")) {
            "Date: ${sharedPreferences.getString("${state}_${visSelected}_LIST_DATE_${listSize - 1}", "No Date")}"
        } else {
            val dateSplit = sharedPreferences.getString("${state}_${visSelected}_LIST_DATE_${listSize - 1}", "No Date")!!.split("T")[0].split("-")
            "Date: ${if (dateSplit[1][0] == '0') dateSplit[1][1] else dateSplit[1]}/${if (dateSplit[2][0] == '0') dateSplit[2][1] else dateSplit[2]}/${dateSplit[0]}"
        }

        // Get totals and daily
        val lineChartSumText = "Total ${visSelected.lowercase(Locale.ROOT)}: ${NumberFormat.getNumberInstance(Locale.US).format(sharedPreferences.getString("${state}_${visSelected}_LIST_SUM_${listSize - 1}", "0")!!.toFloat())}"
        val lineChartDailyText = "${visSelected.lowercase(Locale.ROOT)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}: ${NumberFormat.getNumberInstance(Locale.US).format(sharedPreferences.getString("${state}_${visSelected}_LIST_${listSize - 1}", "0")!!.toFloat())}"

        // Set date, totals, and daily text
        lineChartDateTextView.text = lineChartDateText
        lineChartSumTextView.text = lineChartSumText
        lineChartDailyTextView.text = lineChartDailyText
    }

    /**
     * Draws the [liveChart] based on the Global variable [visSelected] visualization and the [stateSelected] by retrieving COVID-19 data from the phone's [sharedPreferences].
     */
    private fun setLineChart(
        liveChart: LiveChart,
        sharedPreferences: SharedPreferences,
    ) {
        // Initialize empty Mutable Lists to store data points and dates
        val dateRecorded : MutableList<String> = mutableListOf()
        val cumulativeRecorded : MutableList<DataPoint> = mutableListOf()
        val recorded : MutableList<DataPoint> = mutableListOf()

        // Format selected state from "New York" to "NEWYORK" or "All States" to "US"
        val state = if (stateSelected == "All States") "US" else stateSelected.uppercase(Locale.ROOT)
            .replace(" ", "")

        // Get list size from SharedPreferences
        val listSize = sharedPreferences.getInt("${state}_${visSelected}_LIST_SIZE", 0)

        // Add data from SharedPreferences to Mutable Lists
        var skippedData = 0
        for (i in 0 until listSize) {
            val data : Float
            // Skip negative numbers
            if (sharedPreferences.getString("${state}_${visSelected}_LIST_$i", "0")!!.toFloat() >= 0f) {
                // Non-negative number found
                data = sharedPreferences.getString("${state}_${visSelected}_LIST_$i", "0")!!.toFloat()
            } else {
                // Keep track of skipped numbers
                skippedData += 1
                // Continue to next iteration in for-loop
                continue
            }

            // Add all the data of non-negative numbers to the lists
            dateRecorded.add(
                sharedPreferences.getString("${state}_${visSelected}_LIST_DATE_$i", "0")!!
            )
            recorded.add(
                DataPoint((i - skippedData).toFloat(), data)
            )
            cumulativeRecorded.add(
                DataPoint((i - skippedData).toFloat(), sharedPreferences.getString("${state}_${visSelected}_LIST_SUM_$i", "0")!!.toFloat())
            )
        }

        // Initialize DataSet for LineChart
        val mainDataSet = Dataset(recorded)

        // Draw Line Chart
        liveChart
            .setDataset(mainDataSet)
            .setLiveChartStyle(getLineChartStyle())
            .setOnTouchCallbackListener(object : LiveChart.OnTouchCallback {
                /**
                 * Sets the text inside the [liveChart] with matching from the line based on what's found on [sharedPreferences] when touching the [liveChart] by using [point].x as index.
                 * and stops scrolling from ScrollView when touching the chart
                 */
                override fun onTouchCallback(point: DataPoint) {
                    // Convert point.x: Float to Int
                    val i = point.x.roundToInt()

                    // Formats date to MM/DD/YYYY
                    val lineChartDateText = if (dateRecorded[i].contains("/")) {
                        "Date: ${dateRecorded[i]}"
                    } else {
                        val dateSplit = dateRecorded[i].split("T")[0].split("-")
                        "Date: ${if (dateSplit[1][0] == '0') dateSplit[1][1] else dateSplit[1]}/${if (dateSplit[2][0] == '0') dateSplit[2][1] else dateSplit[2]}/${dateSplit[0]}"
                    }

                    // Gets totals and daily numbers
                    val lineChartSumText = "Total ${visSelected.lowercase(Locale.ROOT)}: ${NumberFormat.getNumberInstance(Locale.US).format(cumulativeRecorded[i].y)}"
                    val lineChartDailyText = "${visSelected.lowercase(Locale.ROOT)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}: ${NumberFormat.getNumberInstance(Locale.US).format(recorded[i].y)}"

                    // Sets the LineChart tooltip text
                    lineChartDateTextView.text = lineChartDateText
                    lineChartSumTextView.text = lineChartSumText
                    lineChartDailyTextView.text = lineChartDailyText

                    // Stops screen scrolling when interacting with LineChart
                    liveChart.parent.requestDisallowInterceptTouchEvent(true)
                }

                // Restores screen scrolling when finished interacting with LineChart
                override fun onTouchFinished() {
                    liveChart.parent.requestDisallowInterceptTouchEvent(false)
                }
            })
            .drawBaseline()
            .setBaselineManually(recorded[recorded.size - 1].y)
            .drawSmoothPath()
            .drawDataset()
    }

    /**
     * Returns a LiveChartStyle based on [visSelected]
     */
    private fun getLineChartStyle(): LiveChartStyle {
        return LiveChartStyle().apply {
            // Determines the main color
            mainColor = when (visSelected) {
                "INFECTED" -> {
                    ContextCompat.getColor(this@MainActivity, R.color.orange)
                }
                "VACCINATED" -> {
                    ContextCompat.getColor(this@MainActivity, R.color.green)
                }
                else -> {
                    ContextCompat.getColor(this@MainActivity, R.color.red)
                }
            }

            // Sets colors
            overlayLineColor = ContextCompat.getColor(this@MainActivity, R.color.light_gray2)
            overlayCircleColor = ContextCompat.getColor(this@MainActivity, R.color.gray)
            baselineColor = ContextCompat.getColor(this@MainActivity, R.color.light_gray2)

            // Sets dimensions
            pathStrokeWidth = 4f
            baselineStrokeWidth = 4f
            overlayCircleDiameter = 10f
        }
    }

    /**
     * Sets the text for the Choropleth Map tooltip's TextViews with COVID-19 data based on what's found on [sharedPreferences].
     */
    private fun setInitialChoroplethToolTip(sharedPreferences: SharedPreferences) {
        // Set Choropleth subtitle text
        val choroplethText = "US ${
            visSelected.lowercase(Locale.ROOT)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} Map"
        choroplethTextView.text = choroplethText

        // Format selected state from "New York" to "NEWYORK" or "All States" to "US"
        val state = if (stateSelected == "All States") "US" else stateSelected.uppercase(Locale.ROOT)
            .replace(" ", "")

        // Get list size from SharedPreferences
        val listSize = sharedPreferences.getInt("${state}_${visSelected}_LIST_SIZE", 0)

        // Gets the state selected and totals
        val d3StateText = "State: ${stateSelected.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }}"
        val d3TotalText = "Total ${visSelected.lowercase(Locale.ROOT)}: ${NumberFormat.getNumberInstance(Locale.US).format(sharedPreferences.getString("${state}_${visSelected}_LIST_SUM_${listSize - 1}", "0")!!.toFloat())}"

        // Sets the Choropleth tooltip text
        d3StateTextView.text = d3StateText
        d3TotalTextView.text = d3TotalText
    }

    /**
     * Loads html file in WebView and calls JavaScript function with parameters as strings
     * Array of data values, array of states values, total, stateSelected, visSelected, WebView width, and WebView height.
     * e.g. makeChoroplethMap('[0,1,2,...,10]', '[Alabama,Alaska,...,Wyoming]', '1000000', 'California', 'INFECTED', '900', '600');
     */
    private fun setChoroplethMap(d3WebView : WebView, sharedPreferences: SharedPreferences) {
        // Load WebView file
        d3WebView.loadUrl("file:///android_asset/html/choroplethMap.html")

        // Wait for client to load the file
        d3WebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Get string array states from arrays.xml
                val statesStringArray = resources.getStringArray(R.array.states)
                val states = statesStringArray.toList().toString()

                // Create empty mutable list
                val data = mutableListOf<String>()

                // Get U.S Total numbers of [visSelected]
                val total = sharedPreferences.getString("US_${visSelected}", "0")!!.replace(",", "")

                // Get Total numbers of each state of [visSelected]
                statesStringArray.forEach { state ->
                    // Format selected state from "New York" to "NEWYORK" or "All States" to "US"
                    val selectedFormatted = state.replace(" ", "").uppercase(Locale.US)
                    // Add value to list
                    data.add(sharedPreferences.getString("${selectedFormatted}_${visSelected}", "0")!!.replace(",", ""))
                }

                // Get phone's orientation to avoid sending width as height and height as width
                // based on orientation call JavaScript function with string formatted arguments
                val orientation = resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    d3WebView.loadUrl("javascript:makeChoroplethMap('${data}', '${states}', '${total}', '${visSelected}', '${d3WebView.width}', ${d3WebView.height});")
                } else {
                    d3WebView.loadUrl("javascript:makeChoroplethMap('${data}', '${states}', '${total}', '${visSelected}', '${d3WebView.height}', ${d3WebView.width});")
                }
            }
        }
    }
}