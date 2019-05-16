package ru.touchin

import org.apache.commons.math3.optim.OptimizationData
import org.apache.commons.math3.optim.OptimizationProblem
import ru.touchin.api.models.WiFiScan
import ru.touchin.db.models.OfficePosition
import ru.touchin.db.models.WiFiRouter


object CalculateUtils {

    fun List<WiFiScan>.filterScansAndMapToRouter(officeRouters: List<WiFiRouter>) = filter { wiFiScan ->
        wiFiScan.name.contains("Touch Instinct")
                && officeRouters.any { officeRouter -> officeRouter.macAddress == wiFiScan.macAddress }
    }
        .mapNotNull { wiFiScan ->
            officeRouters.find { officeRouter ->
                officeRouter.macAddress == wiFiScan.macAddress
            }
        }

    fun convertToDistance(levelInDb: Double, frequencyInMhz: Double): Double {
        val exp = (27.55 - 20 * Math.log10(frequencyInMhz) + Math.abs(levelInDb)) / 20.0
        return Math.pow(10.0, exp)
    }

    fun List<Pair<WiFiRouter, Double>>.findBestRouters(): List<Pair<WiFiRouter, Double>> {
        require(this.size >= 3)
        return sortedByDescending { it.second }
            .take(3)
    }

    fun calculateDevicePosition(routersWithDistance: List<Pair<WiFiRouter, Double>>): OfficePosition {
        OptimizationData
    }


}