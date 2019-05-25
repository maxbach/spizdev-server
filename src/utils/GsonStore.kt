package ru.touchin.utils

import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import utils.DateTimeTypeAdapter

object GsonStore {

    val gson = GsonBuilder()
        .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
        .create()

}