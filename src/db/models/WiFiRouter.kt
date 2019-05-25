package ru.touchin.db.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable

object WiFiRouters : IdTable<String>() {
    override val id = varchar("wifi_mac", 50).entityId()
    val positionId = reference("wifi_position", OfficePositions)
}

class WiFiRouterDao(id: EntityID<String>) : Entity<String>(id) {

    companion object : EntityClass<String, WiFiRouterDao>(WiFiRouters, WiFiRouterDao::class.java)

    var position by OfficePositionDao referencedOn WiFiRouters.positionId

    fun toModel() = WiFiRouter(id.value, position.toModel())

}

data class WiFiRouter(
    val macAddress: String,
    val position: OfficePosition
)

