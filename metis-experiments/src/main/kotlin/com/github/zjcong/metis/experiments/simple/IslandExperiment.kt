/*
 * Copyright (C) 2021 Zijie Cong
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

package com.github.zjcong.metis.experiments.simple

import com.formdev.flatlaf.FlatLightLaf
import com.github.zjcong.metis.*
import com.github.zjcong.metis.execution.SimpleExecution
import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.execution.RelayExecution
import com.github.zjcong.metis.execution.RestartExecution
import com.github.zjcong.metis.experiments.benchmark.ContinuousBenchmarkSingleObjectiveProblem
import com.github.zjcong.metis.experiments.benchmark.Rastrigin
import com.github.zjcong.metis.experiments.benchmark.tsp.DANTZIG42
import com.github.zjcong.metis.execution.CompetitiveIslandExecution
import com.github.zjcong.metis.problem.SingleObjectiveProblem
import com.github.zjcong.metis.samplers.*
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.reflect.full.primaryConstructor

/**
 *
 */
fun <T> islandsOf(
    n: Int, singleObjectiveProblem: SingleObjectiveProblem<T>, monitor: Monitor<T>, samplers: List<Sampler>
): List<Execution<T>> {
    return (0 until n).map {
        RestartExecution(
            name = "Island-${samplers[it.rem(samplers.size)].javaClass.simpleName}",
            problem = singleObjectiveProblem,
            sampler = samplers[it.rem(samplers.size)],
            //samplers = samplers,
            monitor = monitor,
            //threshold = problem.dimensions * 10,
            entryPolicy = EntryPolicy.LAST_TO_RANDOM
        )
    }
}


fun <T> experiment(singleObjectiveProblem: SingleObjectiveProblem<T>, population: Int): XYChart? {

    val names = setOf(
        "PSO", "CMAES", "DE", "GA", "Islands", //"Relay"
    )

    val islandNumber = 5

    val simpleExperiment = SimpleExperiment(names) { name, monitor: Monitor<T> ->
        when (name) {
            "DE" -> SimpleExecution(
                name = name,
                problem = singleObjectiveProblem,
                sampler = DifferentialEvolution(singleObjectiveProblem.dimensions),
                monitor = monitor
            )
            "PSO" -> SimpleExecution(
                name = name,
                problem = singleObjectiveProblem,
                sampler = ParticleSwampOptimization(singleObjectiveProblem.dimensions),
                monitor = monitor
            )
            "CMAES" -> SimpleExecution(
                name = name,
                problem = singleObjectiveProblem,
                sampler = CovarianceMatrixAdaption(singleObjectiveProblem.dimensions),
                monitor = monitor
            )
            "GA" -> SimpleExecution(
                name = name,
                problem = singleObjectiveProblem,
                sampler = GeneticAlgorithm(singleObjectiveProblem.dimensions),
                monitor = monitor
            )
            "Islands" -> CompetitiveIslandExecution(
                name = name,
                problem = singleObjectiveProblem,
                monitor = monitor,
                islands = islandsOf(
                    islandNumber, singleObjectiveProblem, monitor, listOf(
                        CovarianceMatrixAdaption(singleObjectiveProblem.dimensions),
                        GeneticAlgorithm(singleObjectiveProblem.dimensions),
                        ParticleSwampOptimization(singleObjectiveProblem.dimensions),
                        DifferentialEvolution(singleObjectiveProblem.dimensions),
                    )
                ),
            )
            "Relay" -> RelayExecution(
                name = name, problem = singleObjectiveProblem, monitor = monitor, samplers = listOf(
                    CovarianceMatrixAdaption(singleObjectiveProblem.dimensions),
                    GeneticAlgorithm(singleObjectiveProblem.dimensions),
                    ParticleSwampOptimization(singleObjectiveProblem.dimensions),
                    DifferentialEvolution(singleObjectiveProblem.dimensions),
                ), threshold = singleObjectiveProblem.dimensions
            )
            else -> throw IllegalStateException("Unexpected engine $name")
        }
    }

    val results = simpleExperiment.start()


    //Fitness Chart
    val convergenceChart = XYChartBuilder().width(1024).height(700)
        .title("${singleObjectiveProblem.dimensions}D ${singleObjectiveProblem.javaClass.simpleName} (p=$population)")
        .xAxisTitle("Iterations")
        .yAxisTitle("Cost (log axis)").theme(Styler.ChartTheme.Matlab).build()

    results.forEach { (name, history) ->
        val series = convergenceChart.addSeries(name, history.map { it.first }, history.map { it.second + 1e-20 })
        series.marker = SeriesMarkers.NONE
    }
    convergenceChart.styler.isYAxisLogarithmic = true
    convergenceChart.styler.xAxisTickMarkSpacingHint = 99
    return convergenceChart
}

/**
 *
 */
fun continuousBenchmarkProblems() {
    val dimensions = 40
    val population = dimensions * 2

    val problems =
        ContinuousBenchmarkSingleObjectiveProblem::class.sealedSubclasses.map { it.primaryConstructor!!.call(dimensions) }
    val charts = problems.map { experiment(it, population) }
    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(charts).displayChartMatrix()
}


fun populationExperiment() {
    val dimensions = 40

    val charts = (50..500 step 50).map { p ->
        experiment(Rastrigin(dimensions), p)
    }
    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(charts).displayChartMatrix()
}

fun tspWithVariousSamplers() {
    val tsp = DANTZIG42()
    val population = 90
    val chart = experiment(tsp, population)
    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(chart).displayChart()
}

fun main() {
    //populationExperiment()
    continuousBenchmarkProblems()
    //tspWithVariousSamplers()
}
