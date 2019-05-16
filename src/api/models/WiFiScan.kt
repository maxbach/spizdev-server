package ru.touchin.api.models

data class WiFiScan(
    val name: String,
    val macAddress: String,
    val frequencyInMhz: Double,
    val levelInDb: Double
)