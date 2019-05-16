package ru.touchin.db.models

import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntIdTable
import ru.touchin.api.models.Stamp

object PhoneStamps : IntIdTable() {
    val phoneId = reference("stamp_phone_id", Phones.id)
    val date = datetime("stamp_date")
    val batteryLevel = float("stamp_battery")
    val gpsPositionId = reference("stamp_gps", GpsPositions).nullable()
    val officePositionId = reference("stamp_office", OfficePositions).nullable()
}

class PhoneStampDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : EntityClass<Int, PhoneStampDao>(PhoneStamps, PhoneStampDao::class.java)

    var phone by PhoneDao referencedOn PhoneStamps.phoneId
    var date by PhoneStamps.date
    var batteryLevel by PhoneStamps.batteryLevel
    var gpsPositionId by GpsPositionDao optionalReferencedOn PhoneStamps.gpsPositionId
    var officePositionId by OfficePositionDao optionalReferencedOn PhoneStamps.officePositionId

}

data class PhoneStamp(
    override val id: Int,
    override val phone: Phone,
    override val batteryLevel: Float,
    override val gpsPosition: GpsPosition?,
    val officePosition: OfficePosition?
) : Stamp(id, phone, batteryLevel, gpsPosition)