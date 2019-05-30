package ru.touchin.requests

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.html.*
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import ru.touchin.db.models.PhoneDao
import ru.touchin.db.models.PhoneStamp
import ru.touchin.db.models.PhoneStampDao
import ru.touchin.db.models.PhoneStamps
import ru.touchin.extensions.getId
import ru.touchin.utils.toJson

object DeviceListPage : BaseRequest() {

    override val path = "/"

    override fun addNewRoute(router: Route) {
        with(router) {
            get(path) {
                val phoneStamps = transaction {
                    PhoneDao.all()
                        .map {
                            it to PhoneStampDao
                                .find { PhoneStamps.phoneId eq it.getId() }
                                .maxBy(PhoneStampDao::date)
                                ?.toModel()
                        }
                }
                call.respondHtml {
                    buildHtmlPage(this, phoneStamps)
                }
            }
        }
    }


    private fun buildHtmlPage(
        html: HTML,
        phoneStamps: List<Pair<PhoneDao, PhoneStamp?>>
    ) {
        with(html) {
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
                                a(DeviceHistoryPage.buildPath(phone.getId())) {
                                    +phone.getId()
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

}