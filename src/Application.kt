package ru.touchin

import com.google.gson.Gson
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
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.css.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import ru.touchin.db.DatabaseController
import ru.touchin.db.models.Phone
import ru.touchin.db.models.PhoneDao
import ru.touchin.db.models.PhoneStampDao
import ru.touchin.db.models.PhoneStamps
import java.text.DateFormat

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val database = DatabaseController
    install(DefaultHeaders)

    routing {
        install(CallLogging)
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

        get("/mobile/list") {
            val phoneStamps = transaction {
                PhoneDao.all()
                    .map {
                        it to PhoneStampDao.find { PhoneStamps.phoneId eq it.id.value }.maxBy { it.date }
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
                                td { +phone.id.value }
                                td { +"${phone.os.name} ${phone.osVersion}" }
                                td { +phone.model }
                                td { +(stamp?.let { Gson().toJson(stamp) } ?: "Нет отпечатков") }
                            }
                        }

                    }
                }
            }
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
