package com.application.covid19.data

import com.google.gson.Gson
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.lang.Exception

import java.net.URL

class StatesInfectedAndDeathsRequest {
    /**
     * companion objects can be though of as attributes, in this case we want an attribute that reflects the URL for our API request
     */
    companion object {
        private const val URL = "https://api.covid19api.com/live/country/united-states"
    }

    /**
     * Fetches [URL] and returns a [StatesInfectedAndDeathsResult]
     */
    fun getResult(): StatesInfectedAndDeathsResult? {

        try{
            val data = URL(URL).readText()
            return Gson().fromJson(data, StatesInfectedAndDeathsResult::class.java)
        }catch (exception: Exception){
            println("testing here")
            println(exception)
        }
        return null

    }

}