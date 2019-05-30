package ru.touchin.utils

import io.ktor.routing.Routing
import ru.touchin.requests.BaseRequest

fun Routing.addRequests(vararg requests: BaseRequest) {
    requests.forEach {
        it.addNewRoute(this)
    }
}