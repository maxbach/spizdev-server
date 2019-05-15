package ru.touchin.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseController {
    private const val DATABASE_PORT = "5432"
    private const val DATABASE_NAME = "admin"
    private const val DATABASE_USER = "admin"
    private const val DATABASE_PASSWORD = "admin"

    init {
        Database.connect(
            "jdbc:postgresql://localhost:$DATABASE_PORT/$DATABASE_NAME",
            driver = "org.postgresql.Driver",
            user = DATABASE_USER,
            password = DATABASE_PASSWORD
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(Phones, WiFiRouters, OfficePositions, GpsPositions, PhoneStamps)
        }

    }


}