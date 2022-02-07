package com.application.covid19.data

import com.google.gson.Gson
import java.lang.Exception
import java.net.URL

class VaccinationsJanssenRequest {
    companion object {
        private const val URL = "https://data.cdc.gov/resource/w9zu-fywh.json"
    }

    fun getResult(): VaccinationsResult?{
        try{
            val data = URL(URL).readText()
            return Gson().fromJson(data, VaccinationsResult::class.java)
        }catch(exception: Exception){
            println(exception)
        }
        return null
    }
}