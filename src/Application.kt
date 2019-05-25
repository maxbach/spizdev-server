package ru.touchin

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveOrNull
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.css.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.event.Level
import ru.touchin.api.models.SendStampBody
import ru.touchin.db.DatabaseController
import ru.touchin.db.models.*
import ru.touchin.utils.toJson
import java.text.DateFormat
import kotlin.math.roundToInt

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private const val GOOGLE_API_KEY = "AIzaSyAhpf9hRIRkNov2uae_Aq07tB-38IN0gPo"

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

        post("/mobile/login") {
            val phone = call.receiveOrNull<Phone>()
            phone?.let {
                transaction {
                    PhoneDao.new(phone)
                    call.respond(HttpStatusCode.OK)
                }
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        post("/mobile/stamp") {
            val body = call.receiveOrNull<SendStampBody>()
            val officeRouters = transaction { WiFiRouterDao.all().toList().map(WiFiRouterDao::toModel) }
            body?.let {
                val phone = transaction { PhoneDao.findById(it.phoneId) }
                if (phone == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@let
                }
                // добавить обработку ошибок с wifi
                val position = CalculateUtils.calculateDevicePosition(
                    body.wiFiScans
                        .filterScansAndMapToRouter(officeRouters)
                        ?.findBestRouters()
                )
                transaction {
                    PhoneStampDao.new {
                        this.phone = phone
                        officePosition = position?.let {
                            OfficePositionDao.new {
                                x = position.first.roundToInt()
                                y = position.second.roundToInt()
                                floor = 5
                            }
                        }
                        batteryLevel = body.batteryLevel
                        date = DateTime.now()
                        gpsPosition = body.gpsPosition?.let(GpsPositionDao.Companion::new)
                    }
                }
                call.respond(HttpStatusCode.OK)

            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        get("/") {
            val phoneStamps = transaction {
                PhoneDao.all()
                    .map {
                        it to PhoneStampDao.find { PhoneStamps.phoneId eq it.id.value }.maxBy { it.date }?.toModel()
                    }
            }
            call.respondHtml {
                body {
                    table {
                        thead {
                            tr {
                                td { +"ID" }
                                td { +"ОС" }
                                td { +"Модель" }
                                td { +"Последний отпечаток" }
                            }
                        }
                        phoneStamps.forEach { (phone, stamp) ->
                            tr {
                                td {
                                    a("/history?phone_id=${phone.id.value}") {
                                        +phone.id.value
                                    }
                                }
                                td { +"${phone.os.name} ${phone.osVersion}" }
                                td { +phone.model }
                                td { +(stamp?.toJson() ?: "Нет отпечатков") }
                            }
                        }

                    }
                }
            }
        }

        get("/history") {
            val phoneId = call.request.queryParameters["phone_id"]
            phoneId?.let {
                val phone = transaction { PhoneDao.findById(phoneId) } ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val history = transaction {
                    PhoneStampDao
                        .find { PhoneStamps.phoneId eq phoneId }
                        .map(PhoneStampDao::toModel)
                }
                call.respondHtml {
                    body {
                        table {
                            thead {
                                tr {
                                    td { +"Время" }
                                    td { +"Позиция в офисе" }
                                    td { +"Позиция в мире" }
                                    td { +"Уровень батареи" }
                                }
                            }
                            history.forEach { stamp ->
                                tr {
                                    td {
                                        a("/history/stamp?stamp_id=${stamp.id}") {
                                            +stamp.date.toString(DateTimeFormat.mediumDateTime())
                                        }
                                    }
                                    td { +(stamp.officePosition?.toJson() ?: "Отсутствует") }
                                    td { +(stamp.gpsPosition?.toJson() ?: "Отсутствует") }
                                    td { +stamp.batteryLevel.toString() }
                                }
                            }
                        }
                    }
                }
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        get("history/stamp") {
            val stampId = call.request.queryParameters["stamp_id"]?.toIntOrNull()
            stampId?.let {
                transaction {
                    PhoneStampDao.findById(stampId)?.toModel()
                }?.let { stamp ->
                    call.respondHtml {
                        head {
                            script(ScriptType.textJavaScript, src = "https://code.jquery.com/jquery-3.4.1.js") {}
                            unsafe {
                                +"<script src=\"https://maps.googleapis.com/maps/api/js?key=$GOOGLE_API_KEY&callback=initMap\" async defer></script>"
                            }
                            script(ScriptType.textJavaScript, src = "/static/snap.svg.js") {}
                            if (stamp.officePosition != null) {
                                script {
                                    +"let pointX = ${stamp.officePosition.x}; let pointY = ${stamp.officePosition.y};"
                                }
                            }
                            script(ScriptType.textJavaScript, src = "/static/script.js") {}
                        }
                        body {
                            +stamp.toJson()
                            div {
                                id = "office_div"
                            }
                            if (stamp.gpsPosition != null && stamp.officePosition == null) {
                                unsafe {
                                    +("<iframe\n" +
                                            "width=\"500\"\n" +
                                            "height=\"500\"\n" +
                                            "frameborder=\"0\" style=\"border:0\"\n" +
                                            "src=\"https://www.google.com/maps/embed/v1/place\n" +
                                            "?key=$GOOGLE_API_KEY\n" +
                                            "&q=${stamp.gpsPosition.latitude},${stamp.gpsPosition.longitude}\n" +
                                            "&zoom=18\">" +
                                            "</iframe>")
                                }
                            }
                        }
                    }
                } ?: call.respond(HttpStatusCode.BadRequest)
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }
        static("static") {
            resources("js")
            resource("office_plan.svg")
        }
    }
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
