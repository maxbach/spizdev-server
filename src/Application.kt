package ru.touchin

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.routing
import org.slf4j.event.Level
import ru.touchin.db.DatabaseController
import ru.touchin.requests.*
import ru.touchin.utils.addRequests
import java.text.DateFormat

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    DatabaseController

    routing {

        install(DefaultHeaders)
        install(CallLogging) {
            level = Level.INFO

        }
        install(StatusPages) {
            exception<IllegalStateException> { cause ->
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        install(ContentNegotiation) {
            gson {
                setDateFormat(DateFormat.LONG)
                setPrettyPrinting()
            }
        }

        addRequests(
            DeviceListPage,
            DeviceHistoryPage,
            MapPage,
            RegisterDeviceRequest,
            NewStampRequest
        )

        static("static") {
            resources("js")
            resource("office_plan.svg")
        }
    }

}

