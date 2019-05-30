package ru.touchin.requests

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.html.*
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import ru.touchin.db.models.GpsPosition
import ru.touchin.db.models.PhoneStamp
import ru.touchin.db.models.PhoneStampDao
import ru.touchin.utils.toJson

object MapPage : BaseRequest() {

    override val path = "/history/stamp"

    private const val STAMP_ID_QUERY_PARAMETER = "stamp_id"
    private const val GOOGLE_API_KEY = "AIzaSyAhpf9hRIRkNov2uae_Aq07tB-38IN0gPo"

    override fun addNewRoute(router: Route) {
        with(router) {
            get(path) {
                val stampId = call.request.queryParameters[STAMP_ID_QUERY_PARAMETER]?.toIntOrNull()
                stampId?.let {
                    transaction {
                        PhoneStampDao.findById(stampId)?.toModel()
                    }?.let { stamp ->
                        call.respondHtml {
                            buildHtmlPage(this, stamp)
                        }
                    } ?: call.respond(HttpStatusCode.BadRequest)
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

    fun buildPath(deviceId: String) = "$path?$STAMP_ID_QUERY_PARAMETER=$deviceId"

    private fun buildHtmlPage(html: HTML, stamp: PhoneStamp) {
        with(html) {
            head {
                addDataThroughScripts(stamp)
                addScripts()
            }
            body {
                +stamp.toJson()
                div {
                    id = "office_div"
                }
                if (stamp.gpsPosition != null && stamp.officePosition == null) {
                    addGoogleMap(stamp.gpsPosition)
                }
            }
        }
    }

    private fun HTMLTag.addGoogleMap(gpsPosition: GpsPosition) = unsafe {
        +("<iframe\n" +
                "width=\"500\"\n" +
                "height=\"500\"\n" +
                "frameborder=\"0\" style=\"border:0\"\n" +
                "src=\"https://www.google.com/maps/embed/v1/place\n" +
                "?key=$GOOGLE_API_KEY\n" +
                "&q=${gpsPosition.latitude},${gpsPosition.longitude}\n" +
                "&zoom=18\">" +
                "</iframe>")
    }

    private fun HEAD.addDataThroughScripts(stamp: PhoneStamp) {
        if (stamp.officePosition != null) {
            script {
                +"let pointX = ${stamp.officePosition.x}; let pointY = ${stamp.officePosition.y};"
            }
        }
        stamp.jsCirclesCode?.let {
            script {
                +it
            }
        }
    }

    private fun HEAD.addScripts() {
        script(ScriptType.textJavaScript, src = "https://code.jquery.com/jquery-3.4.1.js") {}
        unsafe {
            +"<script src=\"https://maps.googleapis.com/maps/api/js?key=$GOOGLE_API_KEY&callback=initMap\" async defer></script>"
        }
        script(ScriptType.textJavaScript, src = "/static/snap.svg.js") {}
        script(ScriptType.textJavaScript, src = "/static/script.js") {}
    }

}