package ru.touchin.db.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import ru.touchin.db.enums.PhoneOs
import ru.touchin.extensions.getId

object Phones : IdTable<String>() {
    override val id = varchar("phone_id", 50).primaryKey().entityId()
    val model = varchar("phone_model", 50)
    val os = enumeration("phone_os", PhoneOs::class)
    val osVersion = varchar("phone_os_version", 10)
}

class PhoneDao(id: EntityID<String>) : Entity<String>(id) {

    companion object : EntityClass<String, PhoneDao>(Phones, PhoneDao::class.java) {
        fun new(phone: Phone) = new(phone.id) {
            model = phone.model
            os = phone.os
            osVersion = phone.osVersion
        }
    }

    var model by Phones.model
    var os by Phones.os
    var osVersion by Phones.osVersion

    fun toModel(): Phone = Phone(getId(), model, os, osVersion)

    fun getFullName() = "$model - ${os.name} $osVersion"

}

data class Phone(
    val id: String,
    val model: String,
    val os: PhoneOs,
    val osVersion: String
) {
    fun getFullName() = "$model - ${os.name} $osVersion"
}