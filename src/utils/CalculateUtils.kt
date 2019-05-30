package ru.touchin

import org.apache.commons.math3.optim.InitialGuess
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.MaxIter
import org.apache.commons.math3.optim.SimpleValueChecker
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizer
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer
import org.apache.commons.math3.random.GaussianRandomGenerator
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.UncorrelatedRandomVectorGenerator
import ru.touchin.api.models.WiFiScan
import ru.touchin.db.models.OfficePosition
import ru.touchin.db.models.WiFiRouter
import kotlin.math.pow
import kotlin.math.roundToInt

private const val ROUTERS_MAX = 3
private const val MIN_GOOD_DISTANCE = 15.0
private const val GOOD_DISTANCE_FOR_ONE_ROUTER = 2.0
private const val MILLIS_IN_METER = 10 * 100

fun List<WiFiScan>.convertToRouters(officeRouters: List<WiFiRouter>): List<Pair<WiFiRouter, Double>> {
    val officeRoutersMacs = officeRouters.map(WiFiRouter::macAddress)
    return filter { wiFiScan ->
        wiFiScan.name.contains("Touch Instinct") && wiFiScan.macAddress in officeRoutersMacs
    }
        .mapNotNull { wiFiScan ->
            val router = officeRouters.find { officeRouter ->
                officeRouter.macAddress == wiFiScan.macAddress
            }
            router?.let { router to CalculateUtils.convertToDistance(wiFiScan.levelInDb, wiFiScan.frequencyInMhz) }
        }
}

fun List<Pair<WiFiRouter, Double>>.findBestRouters(): List<Pair<WiFiRouter, Double>> {
    return sortedBy { it.second }
        .filter { it.second <= MIN_GOOD_DISTANCE * MILLIS_IN_METER }
        .take(ROUTERS_MAX)
}

object CalculateUtils {

    fun convertToDistance(levelInDb: Double, frequencyInMhz: Double): Double {
        val exp = (27.55 - 20.0 * Math.log10(frequencyInMhz) + Math.abs(levelInDb)) / 20.0
        return Math.pow(10.0, exp) * MILLIS_IN_METER
    }

    fun calculateDevicePosition(routersWithDistance: List<Pair<WiFiRouter, Double>>): OfficePosition? {
        if (routersWithDistance.isEmpty()) {
            return null
        } else if (routersWithDistance.size == 1
            && routersWithDistance[0].second <= GOOD_DISTANCE_FOR_ONE_ROUTER * MILLIS_IN_METER
        ) {

            return OfficePosition(
                routersWithDistance[0].first.position.floor,
                routersWithDistance[0].first.position.x,
                routersWithDistance[0].first.position.y,
                routersWithDistance[0].second
            )

        } else {
            return optimizeFunction(routersWithDistance)
        }

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

    private fun optimizeFunction(routersWithDistance: List<Pair<WiFiRouter, Double>>): OfficePosition {

        val bestPoint = MultiStartMultivariateOptimizer(
            NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                SimpleValueChecker(1e-15, 1e-15)
            ),
            10,
            UncorrelatedRandomVectorGenerator(2, GaussianRandomGenerator(JDKRandomGenerator()))
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
            InitialGuess(
                doubleArrayOf(
                    routersWithDistance[0].first.position.x.toDouble(),
                    routersWithDistance[0].first.position.y.toDouble()
                )
            ),
            MaxEval.unlimited(),
            MaxIter.unlimited()
        )
        return OfficePosition(
            5,
            bestPoint.point[0].roundToInt(),
            bestPoint.point[1].roundToInt(),
            bestPoint.value
        )
    }


}