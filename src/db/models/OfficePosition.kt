package ru.touchin.db.models

import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntIdTable

object OfficePositions : IntIdTable() {
    val floor = integer("office_position_floor")
    val x = float("office_position_x")
    val y = float("office_position_y")
    val error = float("office_position_error").nullable()
}

class OfficePositionDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : EntityClass<Int, OfficePositionDao>(OfficePositions, OfficePositionDao::class.java)

    var floor by OfficePositions.floor
    var x by OfficePositions.x
    var y by OfficePositions.y
    var error by OfficePositions.error

}

data class OfficePosition(
    val id: Int,
    val floor: Int,
    val x: Float,
    val y: Float,
    val error: Float
)