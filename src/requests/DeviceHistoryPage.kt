package ru.touchin.requests

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.html.*
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import org.joda.time.format.DateTimeFormat
import ru.touchin.db.models.PhoneDao
import ru.touchin.db.models.PhoneStamp
import ru.touchin.db.models.PhoneStampDao
import ru.touchin.db.models.PhoneStamps
import extensions.toJson

object DeviceHistoryPage : BaseRequest() {

    override val path = "/history"

    private const val DEVICE_ID_QUERY_PARAMETER = "device_id"

    override fun addNewRoute(router: Route) {
        with(router) {
            get(path) {
                val phoneId = call.request.queryParameters[DEVICE_ID_QUERY_PARAMETER]
                phoneId?.let {
                    val phone = transaction { PhoneDao.findById(phoneId) } ?: kotlin.run {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val history = transaction {
                        PhoneStampDao
                            .find { PhoneStamps.phoneId eq phoneId }
                            .map(PhoneStampDao::toModel)
                            .asReversed()
                    }
                    call.respondHtml {
                        buildHtmlPage(this, phone, history)
                    }
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

    fun buildPath(deviceId: String) = "$path?$DEVICE_ID_QUERY_PARAMETER=$deviceId"

    private fun buildHtmlPage(html: HTML, phone: PhoneDao, stampList: List<PhoneStamp>) {
        with(html) {
            body {
                h2 {
                    +phone.getFullName()
                }
                if (stampList.isNotEmpty()) {
                    table {
                        thead {
                            tr {
                                td { +"Время" }
                                td { +"Позиция в офисе" }
                                td { +"Позиция в мире" }
                                td { +"Уровень батареи" }
                            }
                        }
                        stampList.forEach { stamp ->
                            tr {
                                td {
                                    if (stamp.gpsPosition != null || stamp.officePosition != null) {
                                        a(MapPage.buildPath(stamp.id.toString())) {
                                            +stamp.date.toString(DateTimeFormat.shortDateTime())
                                        }
                                    } else {
                                        +stamp.date.toString(DateTimeFormat.mediumDateTime())
                                    }
                                }
                                td { +(stamp.officePosition?.toJson() ?: "Отсутствует") }
                                td { +(stamp.gpsPosition?.toJson() ?: "Отсутствует") }
                                td { +stamp.batteryLevel.toString() }
                            }
                        }
                    }
                } else {
                    p {
                        + "От девайса еще не пришло никаких данных. Пиздите его, он что-то скрывает."
                    }
                }
            }
        }
    }

}