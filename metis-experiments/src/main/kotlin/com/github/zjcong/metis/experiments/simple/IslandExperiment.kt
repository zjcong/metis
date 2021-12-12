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
import com.github.zjcong.metis.execution.DefaultExecution
import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.execution.RelayExecution
import com.github.zjcong.metis.execution.RestartExecution
import com.github.zjcong.metis.experiments.benchmark.ContinuousBenchmarkProblem
import com.github.zjcong.metis.experiments.benchmark.Rastrigin
import com.github.zjcong.metis.experiments.benchmark.tsp.DANTZIG42
import com.github.zjcong.metis.execution.CompetitiveIslandExecution
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
    n: Int, problem: Problem<T>, monitor: Monitor<T>, samplers: List<Sampler>
): List<Execution<T>> {
    return (0 until n).map {
        RestartExecution(
            name = "Island-${samplers[it.rem(samplers.size)].javaClass.simpleName}",
            problem = problem,
            sampler = samplers[it.rem(samplers.size)],
            //samplers = samplers,
            monitor = monitor,
            //threshold = problem.dimensions * 10,
            entryPolicy = EntryPolicy.LAST_TO_RANDOM
        )
    }
}


fun <T> experiment(problem: Problem<T>, population: Int): XYChart? {

    val names = setOf(
        "PSO", "CMAES", "DE", "GA", "Islands", //"Relay"
    )

    val islandNumber = 4

    val simpleExperiment = SimpleExperiment(names) { name, monitor: Monitor<T> ->
        when (name) {
            "DE" -> DefaultExecution(
                name = name, problem = problem, sampler = DifferentialEvolution(problem.dimensions), monitor = monitor
            )
            "PSO" -> DefaultExecution(
                name = name,
                problem = problem,
                sampler = ParticleSwampOptimization(problem.dimensions),
                monitor = monitor
            )
            "CMAES" -> DefaultExecution(
                name = name,
                problem = problem,
                sampler = CovarianceMatrixAdaption(problem.dimensions),
                monitor = monitor
            )
            "GA" -> DefaultExecution(
                name = name, problem = problem, sampler = GeneticAlgorithm(problem.dimensions), monitor = monitor
            )
            "Islands" -> CompetitiveIslandExecution(
                name = name,
                problem = problem,
                monitor = monitor,
                islands = islandsOf(
                    islandNumber, problem, monitor, listOf(
                        CovarianceMatrixAdaption(problem.dimensions),
                        GeneticAlgorithm(problem.dimensions),
                        ParticleSwampOptimization(problem.dimensions),
                        DifferentialEvolution(problem.dimensions),
                    )
                ),
            )
            "Relay" -> RelayExecution(
                name = name, problem = problem, monitor = monitor, samplers = listOf(
                    CovarianceMatrixAdaption(problem.dimensions),
                    GeneticAlgorithm(problem.dimensions),
                    ParticleSwampOptimization(problem.dimensions),
                    DifferentialEvolution(problem.dimensions),
                ), threshold = problem.dimensions
            )
            else -> throw IllegalStateException("Unexpected engine $name")
        }
    }

    val results = simpleExperiment.start()


    //Fitness Chart
    val convergenceChart = XYChartBuilder().width(1024).height(700)
        .title("${problem.dimensions}D ${problem.javaClass.simpleName} (p=$population)").xAxisTitle("Iterations")
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

    val problems = ContinuousBenchmarkProblem::class.sealedSubclasses.map { it.primaryConstructor!!.call(dimensions) }
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
