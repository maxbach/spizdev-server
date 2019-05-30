package ru.touchin.requests

import io.ktor.routing.Route

abstract class BaseRequest {

    abstract val path: String

    abstract fun addNewRoute(router: Route)

}