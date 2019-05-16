package ru.touchin.api.models

import ru.touchin.db.models.GpsPosition
import ru.touchin.db.models.Phone

data class SendStampBody(
    override val id: Int,
    override val phone: Phone,
    override val batteryLevel: Float,
    override val gpsPosition: GpsPosition?,
    val wiFiScans: List<WiFiScan>
) : Stamp(id, phone, batteryLevel, gpsPosition)