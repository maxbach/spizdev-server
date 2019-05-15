package ru.touchin.db.models

import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntIdTable

object GpsPositions : IntIdTable() {
    val x = float("gps_position_x")
    val y = float("gps_position_y")
    val error = float("gps_position_error")
}

class GpsPositionDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : EntityClass<Int, GpsPositionDao>(GpsPositions, GpsPositionDao::class.java)

    var x by GpsPositions.x
    var y by GpsPositions.y
    var error by GpsPositions.error

}

data class GpsPosition(
    val id: Int,
    val x: Float,
    val y: Float,
    val error: Float
)