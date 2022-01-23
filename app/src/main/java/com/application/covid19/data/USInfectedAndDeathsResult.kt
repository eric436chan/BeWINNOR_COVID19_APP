package com.application.covid19.data

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class USInfectedAndDeathsResult : ArrayList<USInfectedAndDeathsItem>(){

    /**
     * Returns total US COVID19 Infected
     */
    fun getInfected(): Int {
        return this[this.size-1].Confirmed
    }

    /**
     * Returns total US COVID19 Deaths
     */
    fun getDeaths(): Int{
        return this[this.size-1].Deaths
    }


    /**
     * Returns last date data was updated
     */
    fun getDateUpdated(): Date {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(this[this.size-1].Date)!!
    }

}