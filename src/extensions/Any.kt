package ru.touchin.utils

fun Any.toJson(): String = GsonStore.gson.toJson(this)