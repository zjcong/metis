/*
 * Copyright (C) Zijie Cong 2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("unused")

package com.github.zjcong.metis.experiments.coco

import Benchmark
import Observer
import Suite

/**
 * COCO Benchmark
 * @param algorithmName name of the algorithm
 * @param algorithmInfo short description of the algorithm
 * @param suiteName name of the suite
 * @param suiteOptions suite options, see coco-doc-C for details
 */
sealed class COCOBenchmark(
    val algorithmName: String,
    algorithmInfo: String,
    suiteName: String,
    suiteOptions: String = "",
    protected val maxEvalMultiplier: Long,
) {

    private val observerName: String = suiteName

    private val observationOptions =
        StringBuilder()
            .append("result_folder: ${algorithmName.uppercase()}_on_$suiteName ")
            .append("algorithm_name: ${algorithmName.uppercase()} ")
            .append("algorithm_info \"$algorithmInfo\"").toString()

    private val suite = Suite(suiteName, "year: 2018", suiteOptions)
    private val observer = Observer(observerName, observationOptions)
    protected val benchmark = Benchmark(suite, observer)

    internal abstract val nextProblem: SingleObjectiveCOCOProblem?
}


/**
 * BBOB unconstrained continuous benchmark
 */
class BBOBBenchmark(
    algorithmName: String,
    algorithmInfo: String,
    suiteOptions: String = "",
    maxEvaluationsMultiplier: Long
) : COCOBenchmark(algorithmName, algorithmInfo, "bbob", suiteOptions, maxEvaluationsMultiplier) {


    override val nextProblem: SingleObjectiveCOCOProblem?
        get() {
            val cocoProblem = benchmark.nextProblem
            if (cocoProblem == null) {
                benchmark.finalizeBenchmark()
                return null
            }
            return ContinuousCOCOProblem(cocoProblem)
        }

    companion object {
        fun onDimensions(
            algorithmName: String, algorithmInfo: String, dimensions: IntArray, maxEvaluations: Long
        ): BBOBBenchmark {
            require(dimensions.all { it in listOf(2, 3, 5, 10, 20, 40) })
            return BBOBBenchmark(
                algorithmName,
                algorithmInfo,
                "dimensions: ${dimensions.joinToString(",")} ",
                maxEvaluations
            )
        }
    }
}

/**
 * BBOB mixed integer benchmark
 */
class BBOBBMixIntBenchmark(
    algorithmName: String,
    algorithmInfo: String,
    suiteOptions: String = "",
    maxEvaluationsMultiplier: Long
) : COCOBenchmark(algorithmName, algorithmInfo, "bbob-mixint", suiteOptions, maxEvaluationsMultiplier) {


    override val nextProblem: SingleObjectiveCOCOProblem?
        get() {
            val cocoProblem = benchmark.nextProblem
            if (cocoProblem == null) {
                benchmark.finalizeBenchmark()
                return null
            }
            return MixIntCOCOProblem(cocoProblem)
        }


    companion object {
        fun onDimensions(
            algorithmName: String,
            algorithmInfo: String,
            dimensions: IntArray,
            maxEvaluations: Long
        ): BBOBBMixIntBenchmark {
            require(dimensions.all { it in listOf(5, 10, 20, 40, 80, 160) })
            return BBOBBMixIntBenchmark(
                algorithmName,
                algorithmInfo,
                "dimensions: ${dimensions.joinToString(",")} ",
                maxEvaluations
            )
        }
    }
}
