package com.application.covid19.data

import kotlin.collections.ArrayList

class VaccinationsResult : ArrayList<VaccinationsItem>(){

    /**
     * Returns a List of [VaccinationsItem] objects from the [state] passed
     */
    fun getVaccines(state : String) : List<VaccinationsItem> {
        return this.filter { ((it.jurisdiction).replace("*", "")).replace(",", "") == state }
    }

}