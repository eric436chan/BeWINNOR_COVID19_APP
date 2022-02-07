package com.application.covid19.data

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.lang.Exception
import java.net.URL
import com.google.gson.Gson

class USInfectedAndDeathsRequest {
    companion object {
        private const val URL = "https://api.covid19api.com/total/country/united-states"
    }



    fun getResult(): USInfectedAndDeathsResult? {

        try{
            val data = URL(URL).readText()
            return Gson().fromJson(data, USInfectedAndDeathsResult::class.java)
        }catch (exception: Exception) {
            println(exception)
        }
        return null
    }
}