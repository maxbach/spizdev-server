package ru.touchin.db.models

import org.jetbrains.exposed.dao.*
import org.joda.time.DateTime

object PhoneStamps : IntIdTable() {
    val phoneId = reference("stamp_phone", Phones)
    val date = datetime("stamp_date")
    val batteryLevel = integer("stamp_battery")
    val gpsPositionId = reference("stamp_gps", GpsPositions).nullable()
    val officePositionId = reference("stamp_office", OfficePositions).nullable()
}

class PhoneStampDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<PhoneStampDao>(PhoneStamps, PhoneStampDao::class.java)

    var phone by PhoneDao referencedOn PhoneStamps.phoneId
    var date by PhoneStamps.date
    var batteryLevel by PhoneStamps.batteryLevel
    var gpsPosition by GpsPositionDao optionalReferencedOn PhoneStamps.gpsPositionId
    var officePosition by OfficePositionDao optionalReferencedOn PhoneStamps.officePositionId

    fun toModel() = PhoneStamp(
        id.value,
        batteryLevel,
        gpsPosition?.toModel(),
        date,
        phone.toModel(),
        officePosition?.toModel()
    )

}

data class PhoneStamp(
    val id: Int,
    val batteryLevel: Int,
    val gpsPosition: GpsPosition?,
    val date: DateTime,
    val phone: Phone,
    val officePosition: OfficePosition?
)