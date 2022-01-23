package com.application.covid19.data

import kotlin.collections.ArrayList

//Of type ArrayList of StatesInfectedAndDeathsItem
class StatesInfectedAndDeathsResult : ArrayList<StatesInfectedAndDeathsItem>(){

    /**
     * Returns list of [StatesInfectedAndDeathsItem] objects based on the state passed
     */
    fun getInfectedAndDeaths(state: String): List<StatesInfectedAndDeathsItem>{
        return this.filter { it.Province == state }.sortedWith(compareBy{it.Date})
    }
}
