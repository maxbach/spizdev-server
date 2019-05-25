package ru.touchin.db.models

import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntIdTable

object OfficePositions : IntIdTable() {
    val floor = integer("office_position_floor")
    val x = integer("office_position_x")
    val y = integer("office_position_y")
    val error = double("office_position_error").nullable()
}

class OfficePositionDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : EntityClass<Int, OfficePositionDao>(OfficePositions, OfficePositionDao::class.java)

    var floor by OfficePositions.floor
    var x by OfficePositions.x
    var y by OfficePositions.y
    var error by OfficePositions.error

    fun toModel() = OfficePosition(
        floor,
        x,
        y,
        error
    )

    fun new(position: OfficePosition) = new {
        floor = position.floor
        x = position.x
        y = position.y
        error = position.error
    }

}

data class OfficePosition(
    val floor: Int,
    val x: Int,
    val y: Int,
    val error: Double?
)