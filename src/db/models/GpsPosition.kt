package ru.touchin.db.models

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object GpsPositions : IntIdTable() {
    val latitude = double("gps_position_lat")
    val longitude = double("gps_position_lng")
    val error = float("gps_position_error")
}

class GpsPositionDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GpsPositionDao>(GpsPositions, GpsPositionDao::class.java) {

        fun new(gps: GpsPosition) = new {
            latitude = gps.latitude
            longitude = gps.longitude
            error = gps.error
        }

    }

    fun toModel() = GpsPosition(latitude, longitude, error)

    var latitude by GpsPositions.latitude
    var longitude by GpsPositions.longitude
    var error by GpsPositions.error

}

data class GpsPosition(
    val latitude: Double,
    val longitude: Double,
    val error: Float
)