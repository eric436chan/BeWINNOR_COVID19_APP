package com.application.covid19.data

import com.google.gson.Gson
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.lang.Exception

import java.net.URL

class StatesInfectedAndDeathsRequest {


    fun getResult(): StatesInfectedAndDeathsResult? {


        try{
            var result : StatesInfectedAndDeathsResult =
                ArrayList<StatesInfectedAndDeathsItem>() as StatesInfectedAndDeathsResult;
            csvReader().open("src/main/res/raw/statesdata.csv") {
                readAllAsSequence().forEach { row ->
                    var temp : StatesInfectedAndDeathsItem = StatesInfectedAndDeathsItem(
                        Integer.valueOf(row[1]),
                        Integer.valueOf(row[2]),
                        row[3],
                        row[0])
                    result.add(temp)
                }
            }
            return result
        }catch (exception: Exception){
            println(exception)
        }
        return null
    }

}