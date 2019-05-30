package ru.touchin.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import ru.touchin.db.models.*

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
            SchemaUtils.drop(Phones, WiFiRouters, OfficePositions, GpsPositions, PhoneStamps)
            SchemaUtils.createMissingTablesAndColumns(Phones, WiFiRouters, OfficePositions, GpsPositions, PhoneStamps)
            transaction {
                if (WiFiRouterDao.all().empty()) {
                    WiFiRouterDao.new("14:cc:20:e5:d6:15") {
                        position = OfficePositionDao.new {
                            x = 3550
                            y = 200
                            floor = 5
                        }
                    }
                    WiFiRouterDao.new("b4:75:0e:38:47:3d") {
                        position = OfficePositionDao.new {
                            x = 17000
                            y = 7000
                            floor = 5
                        }
                    }
                    WiFiRouterDao.new("f4:f2:6d:fb:fb:29") {
                        position = OfficePositionDao.new {
                            x = 30737
                            y = 5700
                            floor = 5
                        }
                    }
                    WiFiRouterDao.new("b4:75:0e:47:c3:0f") {
                        position = OfficePositionDao.new {
                            x = 33500
                            y = 7550
                            floor = 5
                        }
                    }
                    WiFiRouterDao.new("f4:f2:6d:fb:fb:59") {
                        position = OfficePositionDao.new {
                            x = 19817
                            y = 11870
                            floor = 5
                        }
                    }
                    WiFiRouterDao.new("c4:6e:1f:99:b6:7d") {
                        position = OfficePositionDao.new {
                            x = 10700
                            y = 15070
                            floor = 5
                        }
                    }
                }
            }

        }

    }


}