import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.Table
import ru.touchin.db.*
import ru.touchin.db.enums.PhoneOs

class Phone(id: EntityID<String>) : Entity<String>(id) {

    companion object : EntityClass<String, Phone>(Phones, Phone::class.java)

    var model by Phones.model
    var os by Phones.os
    var osVersion by Phones.osVersion

}

class WiFiRouter(id: EntityID<String>) : Entity<String>(id) {

    companion object : EntityClass<String, WiFiRouter>(WiFiRouters, WiFiRouter::class.java)

    var position by OfficePosition referencedOn WiFiRouters.positionId

}

class OfficePosition(id: EntityID<Int>) : IntEntity(id) {

    companion object : EntityClass<Int, OfficePosition>(OfficePositions, OfficePosition::class.java)

    var floor by OfficePositions.floor
    var x by OfficePositions.x
    var y by OfficePositions.y
    var error by OfficePositions.error

}

class GpsPosition(id: EntityID<Int>) : IntEntity(id) {

    companion object : EntityClass<Int, GpsPosition>(GpsPositions, GpsPosition::class.java)

    var x by GpsPositions.x
    var y by GpsPositions.y
    var error by GpsPositions.error

}

class PhoneStamp(id: EntityID<Int>) : IntEntity(id) {

    companion object : EntityClass<Int, PhoneStamp>(PhoneStamps, PhoneStamp::class.java)

    var phone by Phone referencedOn PhoneStamps.phoneId
    var date by PhoneStamps.date
    var batteryLevel by PhoneStamps.batteryLevel
    var gpsPositionId by GpsPosition optionalReferencedOn PhoneStamps.gpsPositionId
    var officePositionId by OfficePosition optionalReferencedOn PhoneStamps.officePositionId

}