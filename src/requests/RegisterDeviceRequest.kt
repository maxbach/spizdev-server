package ru.touchin.requests

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import ru.touchin.db.models.Phone
import ru.touchin.db.models.PhoneDao

object RegisterDeviceRequest : BaseRequest() {
    override val path = "/mobile/login"

    override fun addNewRoute(router: Route) {
        with(router) {
            post(path) {
                val phone = call.receiveOrNull<Phone>()
                phone?.let {
                    transaction {
                        PhoneDao.new(phone)
                        call.respond(HttpStatusCode.OK)
                    }
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

}