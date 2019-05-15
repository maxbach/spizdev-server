package ru.touchin.db

import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.dao.IntIdTable
import ru.touchin.db.enums.PhoneOs

object Phones : IdTable<String>() {
    override val id = varchar("phone_id", 50).entityId().primaryKey()
    val model = varchar("phone_model", 10)
    val os = enumeration("phone_os", PhoneOs::class)
    val osVersion = varchar("phone_os_version", 10)
}

object WiFiRouters : IdTable<String>() {
    override val id = varchar("wifi_id", 50).entityId().primaryKey()
    val positionId = reference("wifi_position", OfficePositions)
}

object OfficePositions : IntIdTable() {
    val floor = integer("office_position_floor")
    val x = float("office_position_x")
    val y = float("office_position_y")
    val error = float("office_position_error").nullable()
}

object GpsPositions : IntIdTable() {
    val x = float("gps_position_x")
    val y = float("gps_position_y")
    val error = float("gps_position_error")
}

object PhoneStamps : IntIdTable() {
    val phoneId = reference("stamp_phone_id", Phones.id)
    val date = datetime("stamp_date")
    val batteryLevel = float("stamp_battery")
    val gpsPositionId = reference("stamp_gps", GpsPositions).nullable()
    val officePositionId = reference("stamp_office", OfficePositions).nullable()
}