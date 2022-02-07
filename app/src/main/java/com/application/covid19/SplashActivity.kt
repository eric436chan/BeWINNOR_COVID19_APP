package com.application.covid19

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.application.covid19.data.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.*
import java.nio.charset.Charset
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SplashActivity : AppCompatActivity() {

    /**
     * API Request from https://data.cdc.gov/
     * [VaccinationsPfizerRequest], [VaccinationsModernaRequest], and [VaccinationsJanssenRequest]
     * Each will Return an ArrayList<USInfectedAndDeathsItem>()
     * Which we can combine into a single ArrayList<USInfectedAndDeathsItem>() for Data in the U.S
     * Then create a mutableMapOf<String, List<VaccinationsItem>>() to filter data of each U.S State as a Map item e.g. ("New York", List<VaccinationsItem>)
     * Then pass it to [addDATACDCToSharedPreferences]
     *
     * API Request from https://api.covid19api.com/
     * [USInfectedAndDeathsRequest], and [StatesInfectedAndDeathsRequest]
     * Which we can combine into a single ArrayList<USInfectedAndDeathsItem>() for Data in the U.S
     * Then create a mutableMapOf<String, ArrayList<StatesInfectedAndDeathsItem>>() to filter data of each U.S State as a Map item e.g. ("New York", List<StatesInfectedAndDeathsItem>)
     * Then pass it to [addCOVID19APIToSharedPreferences]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sets policy to allow running network on main thread
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        // Get String Array states from arrays.xml
        val states = resources.getStringArray(R.array.states)

        // Request data from APIs
        doAsync {
            uiThread {
                // Request data from https://data.cdc.gov/
                try {
                    // Request Pfizer, Moderna, and Janssen vaccinations data
                    val pfizerUSAndStatesVaccinations = VaccinationsPfizerRequest().getResult()!!
                    val modernaUSAndStatesVaccinations = VaccinationsModernaRequest().getResult()!!
                    val janssenUSAndStatesVaccinations = VaccinationsJanssenRequest().getResult()!!

                    // Combine Pfizer, Moderna, and Janssen vaccinations totals
                    val usCombinedVaccinations = (pfizerUSAndStatesVaccinations + modernaUSAndStatesVaccinations + janssenUSAndStatesVaccinations)
                        .sortedWith(compareBy { it.week_of_allocations })

                    // Initialize an empty map of <String, List<VaccinationsItem>>
                    val statesVaccinatedOverTime = mutableMapOf<String, List<VaccinationsItem>>()

                    // Add data to map as <"California", List<VaccinationsItem>>
                    states.forEach { state ->
                        if (state != "All States") {
                            statesVaccinatedOverTime[state] = (pfizerUSAndStatesVaccinations.getVaccines(state) + modernaUSAndStatesVaccinations.getVaccines(state) + janssenUSAndStatesVaccinations.getVaccines(state))
                                .sortedWith(compareBy { it.week_of_allocations })
                        }
                    }

                    // Add data collected to Shared Preferences
                    addDATACDCToSharedPreferences(usCombinedVaccinations, statesVaccinatedOverTime)
                } catch (e: Exception) {
                    println("testing here")
                    println(e)
                    Toast.makeText(this@SplashActivity, "CDC API Failed to Respond", Toast.LENGTH_SHORT).show()
                }

                // Request data from https://api.covid19api.com/
                try {
                    // Request infected and deaths data for the US
                    val usInfectedAndDeaths = USInfectedAndDeathsRequest().getResult()!!

                    // Request infected and deaths data for all states
                    val statesInfectedAndDeaths = StatesInfectedAndDeathsRequest().getResult()!!

                    // Initialize an empty map of <String, List<StatesInfectedAndDeathsItem>>
                    val statesInfectedAndDeathsOverTime = mutableMapOf<String, List<StatesInfectedAndDeathsItem>>()

                    // Add data to map as <"California", List<StatesInfectedAndDeathsItem>>
                    states.forEach { state ->
                        statesInfectedAndDeathsOverTime[state] = statesInfectedAndDeaths.getInfectedAndDeaths(state)
                    }

                    // Add data collected to Shared Preferences
                    addCOVID19APIToSharedPreferences(usInfectedAndDeaths, statesInfectedAndDeathsOverTime)
                } catch (e: Exception) {
                    println(e)
                    Toast.makeText(this@SplashActivity, "COVID19 API Failed to Respond", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Remove title bar
        if (supportActionBar != null)
            supportActionBar?.hide()

        // Suppress dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Start Main Activity after all collected data has been saved in Shared Preferences
        startActivity(Intent(this, MainActivity::class.java))
        this.finish()
    }

    /**
     * Adds COVID-19 DATA retrieved from https://data.cdc.gov/ and stores it in the phone's Shared Preferences
     * Calls [addDATACDCToSharedPreferencesSTATES] for states data and [addDATACDCToSharedPreferencesUS] for the U.S Data
     */
    private fun addDATACDCToSharedPreferences(
        usCombinedVaccinations: List<VaccinationsItem>,
        statesVaccinatedOverTime: MutableMap<String, List<VaccinationsItem>>
    ) {
        // Access to the phone's Shared Preferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("COVID_19", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get String Array states from arrays.xml
        val states = resources.getStringArray(R.array.states)

        // Add vaccination data collected from all states
        addDATACDCToSharedPreferencesSTATES(editor, states, statesVaccinatedOverTime)

        // Add vaccination data collected from the US
        addDATACDCToSharedPreferencesUS(editor, usCombinedVaccinations)

        // Apply changes for the Shared Preferences editor
        editor.apply()
    }

    /**
     * Adds COVID-19 DATA retrieved from https://api.covid19api.com/ and stores it in the phone's Shared Preferences
     * Calls [addCOVID19APIToSharedPreferencesSTATES] for states data and [addCOVID19APIToSharedPreferencesUS] for the U.S Data
     * It also calls [getMissingInfectedAndDeathData] for data before 01/15/2021
     */
    private fun addCOVID19APIToSharedPreferences(
        usInfectedAndDeaths: USInfectedAndDeathsResult,
        statesInfectedAndDeathsOverTime: MutableMap<String, List<StatesInfectedAndDeathsItem>>
    ) {
        // Access to the phone's Shared Preferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("COVID_19", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get String Array states from arrays.xml
        val states = resources.getStringArray(R.array.states)

        // Adds infected and deaths data before 01/15/2021
        val oldDataStateSizes = getMissingInfectedAndDeathData(editor)

        // Add infected and deaths data collected from all states
        addCOVID19APIToSharedPreferencesSTATES(sharedPreferences, editor, states, statesInfectedAndDeathsOverTime, oldDataStateSizes)

        // Add infected and deaths data collected from the US
        addCOVID19APIToSharedPreferencesUS(editor, usInfectedAndDeaths)

        // Apply changes for the Shared Preferences editor
        editor.apply()
    }

    /**
     * READ!
     * This project uses the phone's Shared Preferences to save data retrieved from APIs locally to avoid total dependency of the APIs
     * We save an imitation of a list using pure Strings by saving all the needed attributes of a simple List
     * This same method is use through the project and it follows the naming convention:
     *     "[STATE]_[DATA TYPE]_LIST_[INDEX] = DATA"
     *     "[STATE]_[DATA TYPE]_LIST_SUM_[INDEX] = DATA"
     *     "[STATE]_[DATA TYPE]_LIST_DATE_[INDEX] = DATA"
     *     "[STATE]_[DATA TYPE]_LIST_SIZE = DATA"
     *
     * e.g "NEWYORK_INFECTED_LIST_0 = 1, NEWYORK_INFECTED_LIST_1 = 12, ..."
     *     "NEWYORK_INFECTED_LIST_SUM_0 = 1, NEWYORK_INFECTED_LIST_SUM = 13, ..."
     *     "NEWYORK_INFECTED_LIST_DATE_0 = 2020-08-22, NEWYORK_INFECTED_LIST_DATE_1 = 2020-08-23, ..."
     *     "NEWYORK_INFECTED_LIST_SIZE = 150"
     */

    /**
     * Adds U.S COVID-19 Vaccinations DATA retrieved from https://data.cdc.gov/
     * and stores it in the phone's Shared Preferences using its [editor]
     */
    private fun addDATACDCToSharedPreferencesUS(
        editor: SharedPreferences.Editor,
        usCombinedVaccinations: List<VaccinationsItem>
    ) {
        // Get list size
        val usVaccinatedListSize = usCombinedVaccinations.size

        // Reduce U.S vaccinations to a map of <date: String, vaccinations: Int>
        val usReducedVaccinations = mutableMapOf<String, Int>()

        // Iterate the list and group vaccine totals per dates
        for (i in 0 until usVaccinatedListSize) {
            if (usCombinedVaccinations[i].week_of_allocations in usReducedVaccinations) {
                usReducedVaccinations[usCombinedVaccinations[i].week_of_allocations] = usReducedVaccinations[usCombinedVaccinations[i].week_of_allocations]!! + usCombinedVaccinations[i]._1st_dose_allocations.toInt()
            } else {
                usReducedVaccinations[usCombinedVaccinations[i].week_of_allocations] = usCombinedVaccinations[i]._1st_dose_allocations.toInt()
            }
        }

        // Variables to calculate vaccinations
        var usVaccinatedTotal = 0
        var usVaccinatedDifference = 0
        var usVaccinatedIndex = 1

        // Add first entry to be 0
        editor.putString("US_VACCINATED_LIST_${0}", "0")
        editor.putString("US_VACCINATED_LIST_SUM_${0}", "0")
        editor.putString("US_VACCINATED_LIST_DATE_${0}", "2020-12-20T00:00:00.000")

        // Add data in reduced US vaccinations to shared preferences
        usReducedVaccinations.forEach { (date, vaccinations) ->
            // Calculate vaccinations totals and new vaccinations
            usVaccinatedDifference = vaccinations
            usVaccinatedTotal += vaccinations

            editor.putString("US_VACCINATED_LIST_${usVaccinatedIndex}", vaccinations.toString())
            editor.putString("US_VACCINATED_LIST_SUM_${usVaccinatedIndex}", usVaccinatedTotal.toString())
            editor.putString("US_VACCINATED_LIST_DATE_${usVaccinatedIndex}", date)

            // Increment index
            usVaccinatedIndex++
        }

        // Add US vaccinated list size to Shared preferences
        editor.putInt("US_VACCINATED_LIST_SIZE", usVaccinatedIndex)

        // Add US vaccinated total to Shared Preferences
        editor.putString("US_VACCINATED", NumberFormat.getNumberInstance(Locale.US).format(usVaccinatedTotal).toString())
        editor.putString("US_NEW_VACCINATED", NumberFormat.getNumberInstance(Locale.US).format(usVaccinatedDifference).toString())
    }

    /**
     * Adds every U.S State COVID-19 Vaccinations DATA retrieved from https://data.cdc.gov/
     * and stores it in the phone's Shared Preferences using its [editor]
     */
    private fun addDATACDCToSharedPreferencesSTATES(
        editor: SharedPreferences.Editor,
        states: Array<String>,
        statesVaccinatedOverTime: MutableMap<String, List<VaccinationsItem>>
    ) {
        states.forEach { state ->
            if (state != "All States") {
                // Format state from "New York" to "NEWYORK"
                val stateFormatted = state.replace(" ", "").uppercase(Locale.ROOT)

                // Get the state vaccinated list
                val stateCombinedVaccinations = statesVaccinatedOverTime[state]!!

                // Get list size
                val stateVaccinatedListSize = stateCombinedVaccinations.size

                // Reduce state vaccinated to a map of <date: String, vaccinations: Int>
                val statesReducedVaccinations = mutableMapOf<String, Int>()

                // Iterate the list and group vaccine totals per dates
                for (i in 0 until stateVaccinatedListSize) {
                    if (stateCombinedVaccinations[i].week_of_allocations in statesReducedVaccinations) {
                        statesReducedVaccinations[stateCombinedVaccinations[i].week_of_allocations] = statesReducedVaccinations[stateCombinedVaccinations[i].week_of_allocations]!! + stateCombinedVaccinations[i]._1st_dose_allocations.toInt()
                    } else {
                        statesReducedVaccinations[stateCombinedVaccinations[i].week_of_allocations] = stateCombinedVaccinations[i]._1st_dose_allocations.toInt()
                    }
                }

                // Variables to calculate vaccinations
                var stateVaccinatedTotal = 0
                var stateVaccinatedNew = 0
                var stateVaccinatedIndex = 1

                // Add first entry to be 0
                editor.putString("${stateFormatted}_VACCINATED_LIST_${0}", "0")
                editor.putString("${stateFormatted}_VACCINATED_LIST_SUM_${0}", "0")
                editor.putString("${stateFormatted}_VACCINATED_LIST_DATE_${0}", "2020-12-20T00:00:00.000")

                // Add data in reduced state vaccinations to shared preferences
                statesReducedVaccinations.forEach { (date, vaccinations) ->
                    // Calculate vaccinations totals and new vaccinations
                    stateVaccinatedNew = vaccinations
                    stateVaccinatedTotal += vaccinations

                    editor.putString("${stateFormatted}_VACCINATED_LIST_${stateVaccinatedIndex}", vaccinations.toString())
                    editor.putString("${stateFormatted}_VACCINATED_LIST_SUM_${stateVaccinatedIndex}", stateVaccinatedTotal.toString())
                    editor.putString("${stateFormatted}_VACCINATED_LIST_DATE_${stateVaccinatedIndex}", date)

                    // Increment index
                    stateVaccinatedIndex++
                }

                // Add state vaccinated list size to Shared preferences
                editor.putInt("${stateFormatted}_VACCINATED_LIST_SIZE", stateVaccinatedIndex)

                // Add state vaccinated total to Shared preferences
                editor.putString("${stateFormatted}_VACCINATED", NumberFormat.getNumberInstance(Locale.US).format(stateVaccinatedTotal).toString())
                editor.putString("${stateFormatted}_NEW_VACCINATED", NumberFormat.getNumberInstance(Locale.US).format(stateVaccinatedNew).toString())
            }
        }
    }

    /**
     * Adds U.S COVID-19 Infected and Deaths DATA retrieved from https://api.covid19api.com/
     * and stores it in the phone's Shared Preferences using its [editor]
     */
    private fun addCOVID19APIToSharedPreferencesUS(
        editor: SharedPreferences.Editor,
        usInfectedAndDeaths: USInfectedAndDeathsResult
    ) {
        // Get list size
        val usListSize = usInfectedAndDeaths.size

        // Variables to calculate infected and deaths
        var infectedDifference = 0
        var deathDifference = 0
        var infectedTotal = 0
        var deathTotal = 0

        for (i in 0 until usListSize) {
            // Calculate daily total
            infectedDifference = (usInfectedAndDeaths[i].Confirmed - infectedTotal)
            deathDifference = (usInfectedAndDeaths[i].Deaths - deathTotal)

            // Get cumulative totals
            infectedTotal = usInfectedAndDeaths[i].Confirmed
            deathTotal = usInfectedAndDeaths[i].Deaths

            // Add daily values to shared preferences
            editor.putString("US_INFECTED_LIST_$i", (infectedDifference).toString())
            editor.putString("US_DEATHS_LIST_$i", (deathDifference).toString())

            // Add cumulative total to Shared Preferences
            editor.putString("US_INFECTED_LIST_SUM_$i", infectedTotal.toString())
            editor.putString("US_DEATHS_LIST_SUM_$i", deathTotal.toString())

            // Add dates to Shared Preferences
            editor.putString("US_INFECTED_LIST_DATE_$i", usInfectedAndDeaths[i].Date)
            editor.putString("US_DEATHS_LIST_DATE_$i", usInfectedAndDeaths[i].Date)
        }

        // Add US infected and deaths list size to Shared Preferences
        editor.putInt("US_INFECTED_LIST_SIZE", usListSize)
        editor.putInt("US_DEATHS_LIST_SIZE", usListSize)

        // Add US infected and deaths totals to Shared Preferences
        editor.putString("US_UPDATED", "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.US).format(usInfectedAndDeaths.getDateUpdated())}")
        editor.putString("US_NEW_INFECTED", NumberFormat.getNumberInstance(Locale.US).format(infectedDifference))
        editor.putString("US_INFECTED", NumberFormat.getNumberInstance(Locale.US).format(usInfectedAndDeaths.getInfected()))
        editor.putString("US_NEW_DEATHS", NumberFormat.getNumberInstance(Locale.US).format(deathDifference))
        editor.putString("US_DEATHS", NumberFormat.getNumberInstance(Locale.US).format(usInfectedAndDeaths.getDeaths()))
    }

    /**
     * Adds every U.S State Infected and Deaths DATA missing
     * which is retrieved from the New York Times COVID-19 Data in the U.S
     * and stores it in the phone's Shared Preferences using its [editor]
     * Returns a map of <state: String, listSize: Int>
     */
    private fun getMissingInfectedAndDeathData(
        editor: SharedPreferences.Editor,
    ): MutableMap<String, Int> {
        // Open csv file from the res/raw folder
        val inputStream = resources.openRawResource(R.raw.statesdata)
        // Create a bufferReader to read each line in the csv file
        val bufferReader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        // Create a map to store the size of each state data <state: String, listSize: Int>
        val stateDataSize: MutableMap<String, Int> = mutableMapOf()

        try {
            // Read the first file line
            bufferReader.readLine()
            // Line currently being read
            var line = bufferReader.readLine()
            // Declare a list to store the line results separated by a comma
            var lineSeparated : List<String>
            // Initialize index
            var i = 0

            // Declare variables for storing the previous line infected, deaths, and state
            var lastState = "ALABAMA"
            var lastInfectedDaily : Int
            var lastDeathsDaily : Int

            // Initialize variables for storing the current line infected, deaths, and state
            var state = "ALABAMA"
            var infectedDaily = 0
            var deathsDaily = 0

            // Read every line
            while (line != null) {
                // Split string by comma
                lineSeparated = line.split(",")

                // Keep track of the previous line infected, deaths, and state
                lastState = state
                lastInfectedDaily = infectedDaily
                lastDeathsDaily = deathsDaily

                // Current infected, deaths, and state
                state = lineSeparated[0].replace(" ", "").uppercase(Locale.ROOT)
                infectedDaily = lineSeparated[1].toInt()
                deathsDaily = lineSeparated[2].toInt()

                // Add current line infected data
                editor.putString("${state}_INFECTED_LIST_${i}", (infectedDaily - lastInfectedDaily).toString())
                editor.putString("${state}_INFECTED_LIST_SUM_${i}", lineSeparated[1])
                editor.putString("${state}_INFECTED_LIST_DATE_${i}", lineSeparated[3])

                // Add current line deaths data
                editor.putString("${state}_DEATHS_LIST_${i}", (deathsDaily - lastDeathsDaily).toString())
                editor.putString("${state}_DEATHS_LIST_SUM_${i}", lineSeparated[2])
                editor.putString("${state}_DEATHS_LIST_DATE_${i}", lineSeparated[3])

                // If state is different set index to 0 and store the size of the previous state data
                if (state != lastState ) {
                    stateDataSize[lastState] = i
                    i = 0
                }

                // Increase index and read next line
                i += 1
                line = bufferReader.readLine()
            }
            // Store the size of the last state data
            stateDataSize[lastState] = i
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bufferReader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return stateDataSize
        }
    }

    /**
     * Adds every U.S State COVID-19 Infected and Deaths DATA retrieved from https://api.covid19api.com/
     * and stores it in the phone's Shared Preferences using its [editor]
     */
    private fun addCOVID19APIToSharedPreferencesSTATES(
        sharedPreferences: SharedPreferences,
        editor: SharedPreferences.Editor,
        states: Array<String>,
        statesInfectedAndDeathsOverTime: MutableMap<String, List<StatesInfectedAndDeathsItem>>,
        oldDataStateSizes: MutableMap<String, Int>
    ) {
        states.forEach { state ->
            if (state != "All States") {
                // Format state from "New York" to "NEWYORK"
                val stateFormatted = state.replace(" ", "").uppercase(Locale.ROOT)

                // Get the state infected and deaths list
                val infectedAndDeaths = statesInfectedAndDeathsOverTime[state]!!

                // Get list sizes
                val stateInfectedAndDeathsOldListSize = oldDataStateSizes[stateFormatted]!!
                val stateInfectedAndDeathsNewListSize = infectedAndDeaths.size

                // Get previous infected and deaths
                val prevInfectedAndDeaths = infectedAndDeaths[infectedAndDeaths.size - 1]

                // Variables to calculate infected and deaths
                var stateInfectedDifference = 0
                var stateDeathsDifference = 0
                var stateInfectedTotal = sharedPreferences.getString(
                    "${stateFormatted}_INFECTED_LIST_SUM_${stateInfectedAndDeathsOldListSize - 1}",
                    "0"
                )!!.toInt()
                var stateDeathsTotal = sharedPreferences.getString(
                    "${stateFormatted}_DEATHS_LIST_SUM_${stateInfectedAndDeathsOldListSize - 1}",
                    "0"
                )!!.toInt()

                for (i in 0 until stateInfectedAndDeathsNewListSize) {
                    // Calculate infected and deaths totals and new infected and deaths
                    stateInfectedDifference = if (infectedAndDeaths[i].Confirmed - stateInfectedTotal > 0) infectedAndDeaths[i].Confirmed - stateInfectedTotal else -(infectedAndDeaths[i].Confirmed - stateInfectedTotal)
                    stateDeathsDifference = if (infectedAndDeaths[i].Deaths - stateDeathsTotal > 0) infectedAndDeaths[i].Deaths - stateDeathsTotal else -(infectedAndDeaths[i].Deaths - stateDeathsTotal)
                    stateInfectedTotal = infectedAndDeaths[i].Confirmed
                    stateDeathsTotal = infectedAndDeaths[i].Deaths

                    editor.putString("${stateFormatted}_INFECTED_LIST_${stateInfectedAndDeathsOldListSize + i}", stateInfectedDifference.toString())
                    editor.putString("${stateFormatted}_INFECTED_LIST_SUM_${stateInfectedAndDeathsOldListSize + i}", stateInfectedTotal.toString())
                    editor.putString("${stateFormatted}_INFECTED_LIST_DATE_${stateInfectedAndDeathsOldListSize + i}", infectedAndDeaths[i].Date)

                    editor.putString("${stateFormatted}_DEATHS_LIST_${stateInfectedAndDeathsOldListSize + i}", stateDeathsDifference.toString())
                    editor.putString("${stateFormatted}_DEATHS_LIST_SUM_${stateInfectedAndDeathsOldListSize + i}", stateDeathsTotal.toString())
                    editor.putString("${stateFormatted}_DEATHS_LIST_DATE_${stateInfectedAndDeathsOldListSize + i}", infectedAndDeaths[i].Date)
                }

                // Add infected and deaths list size to Shared Preferences
                editor.putInt("${stateFormatted}_INFECTED_LIST_SIZE", (stateInfectedAndDeathsNewListSize + stateInfectedAndDeathsOldListSize))
                editor.putInt("${stateFormatted}_DEATHS_LIST_SIZE", (stateInfectedAndDeathsNewListSize + stateInfectedAndDeathsOldListSize))

                // Add state infected and deaths total to Shared preferences
                editor.putString("${stateFormatted}_UPDATED", "Last updated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.US)
                    .format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(prevInfectedAndDeaths.Date)!!)}")
                editor.putString("${stateFormatted}_INFECTED", NumberFormat.getNumberInstance(Locale.US).format(prevInfectedAndDeaths.Confirmed))
                editor.putString("${stateFormatted}_DEATHS", NumberFormat.getNumberInstance(Locale.US).format(prevInfectedAndDeaths.Deaths))
                editor.putString("${stateFormatted}_NEW_INFECTED", NumberFormat.getNumberInstance(Locale.US).format(stateInfectedDifference))
                editor.putString("${stateFormatted}_NEW_DEATHS", NumberFormat.getNumberInstance(Locale.US).format(stateDeathsDifference))
            }
        }
    }
}