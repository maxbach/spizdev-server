package ru.touchin.api.models

import ru.touchin.db.models.GpsPosition
import ru.touchin.db.models.Phone

abstract class Stamp(
    open val id: Int,
    open val batteryLevel: Float,
    open val gpsPosition: GpsPosition?
)