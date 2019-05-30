package ru.touchin.requests

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import org.joda.time.DateTime
import ru.touchin.CalculateUtils
import ru.touchin.api.models.SendStampBody
import ru.touchin.convertToRouters
import ru.touchin.db.models.*
import ru.touchin.findBestRouters

object NewStampRequest : BaseRequest() {
    override val path = "/mobile/stamp"

    override fun addNewRoute(router: Route) {
        with(router) {
            post(path) {
                val body = call.receiveOrNull<SendStampBody>()
                val officeRouters = transaction { WiFiRouterDao.all().toList().map(WiFiRouterDao::toModel) }
                body?.let {
                    val phone = transaction { PhoneDao.findById(it.phoneId) }
                    if (phone == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@let
                    }
                    // добавить обработку ошибок с wifi
                    val routers = body.wiFiScans
                        .convertToRouters(officeRouters)
                        .findBestRouters()

                    val position = CalculateUtils.calculateDevicePosition(routers)

                    transaction {
                        PhoneStampDao.new {
                            this.phone = phone
                            officePosition = position?.let(OfficePositionDao.Companion::new)
                            batteryLevel = body.batteryLevel
                            date = DateTime.now()
                            gpsPosition = body.gpsPosition?.let(GpsPositionDao.Companion::new)
                            routers.takeIf { it.isNotEmpty() }?.let {
                                jsCode = routers.mapIndexed { index, pair ->
                                    "let circle${index}X = ${pair.first.position.x};" +
                                            "let circle${index}Y = ${pair.first.position.y};" +
                                            "let circle${index}R = ${pair.second};"
                                }.joinToString(separator = "")
                            }
                        }
                    }

                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

}