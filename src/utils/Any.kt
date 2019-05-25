package ru.touchin.utils

import com.google.gson.Gson

fun Any.toJson(): String = GsonStore.gson.toJson(this)