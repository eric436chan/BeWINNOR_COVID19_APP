package com.application.covid19.data

import java.net.URL
import com.google.gson.Gson
import java.lang.Exception

class VaccinationsPfizerRequest {
    companion object {
        private const val URL = "https://data.cdc.gov/resource/saz5-9hgg.json"
    }

    fun getResult():VaccinationsResult?{
        try{
            val data = URL(URL).readText()
            return Gson().fromJson(data, VaccinationsResult::class.java)
        }catch (exception:Exception){
            println(exception)
        }
        return null
    }
}