package ru.touchin

import org.apache.commons.math3.optim.*
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer
import ru.touchin.api.models.WiFiScan
import ru.touchin.db.models.WiFiRouter
import kotlin.math.pow

private const val ROUTERS_MIN = 3

fun List<WiFiScan>.filterScansAndMapToRouter(officeRouters: List<WiFiRouter>): List<Pair<WiFiRouter, Double>>? {
    val officeRoutersMacs = officeRouters.map(WiFiRouter::macAddress)
    return filter { wiFiScan ->
        wiFiScan.name.contains("Touch Instinct") && wiFiScan.macAddress in officeRoutersMacs
    }
        .takeIf { it.size >= ROUTERS_MIN }
        ?.mapNotNull { wiFiScan ->
            val router = officeRouters.find { officeRouter ->
                officeRouter.macAddress == wiFiScan.macAddress
            }
            router?.let { router to CalculateUtils.convertToDistance(wiFiScan.levelInDb, wiFiScan.frequencyInMhz) }
        }
}

fun List<Pair<WiFiRouter, Double>>.findBestRouters(): List<Pair<WiFiRouter, Double>> {
    require(this.size >= ROUTERS_MIN)
    return sortedBy { it.second }
        .take(ROUTERS_MIN)
}

object CalculateUtils {

    fun convertToDistance(levelInDb: Double, frequencyInMhz: Double): Double {
        val exp = (27.55 - 20 * Math.log10(frequencyInMhz) + Math.abs(levelInDb)) / 20.0
        return Math.pow(10.0, exp)
    }

    fun calculateDevicePosition(routersWithDistance: List<Pair<WiFiRouter, Double>>?): Pair<Double, Double>? {
        if (routersWithDistance == null) return null
        require(routersWithDistance.size == ROUTERS_MIN)
        val bestPoint = NonLinearConjugateGradientOptimizer(
            NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
            SimpleValueChecker(1e-15, 1e-15)
        ).optimize(
            ObjectiveFunctionGradient {
                val x = it[0]
                val y = it[1]
                routersWithDistance
                    .map { calculateGradient(it.first, it.second, x, y) }
                    .fold(doubleArrayOf(0.0, 0.0)) { sum, item -> doubleArrayOf(sum[0] + item[0], sum[1] + item[1]) }
            },
            GoalType.MINIMIZE,
            ObjectiveFunction {
                val x = it[0]
                val y = it[1]
                routersWithDistance.sumByDouble { calculateFunction(it.first, it.second, x, y) }
            },
            InitialGuess(doubleArrayOf(
                0.0, 0.0
            )),
            MaxEval.unlimited(),
            MaxIter.unlimited()
        )
        return bestPoint.point[0] to bestPoint.point[1]
    }

    private fun calculateFunction(router: WiFiRouter, distance: Double, x: Double, y: Double) = Math.abs(
        (x - router.position.x).pow(2) + (y - router.position.y).pow(2) - distance.pow(2)
    )

    private fun calculateGradient(router: WiFiRouter, distance: Double, x: Double, y: Double): DoubleArray {
        val signum = Math.signum(calculateFunction(router, distance, x, y))
        return doubleArrayOf(
            2 * (x - router.position.x) * signum,
            2 * (y - router.position.y) * signum
        )
    }


}