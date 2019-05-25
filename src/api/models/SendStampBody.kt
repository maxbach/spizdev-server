package ru.touchin.api.models

import ru.touchin.db.models.GpsPosition

data class SendStampBody(
    val batteryLevel: Int,
    val gpsPosition: GpsPosition?,
    val phoneId: String,
    val wiFiScans: List<WiFiScan>
)