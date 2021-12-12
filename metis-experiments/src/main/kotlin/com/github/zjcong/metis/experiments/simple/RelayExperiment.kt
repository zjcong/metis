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

package com.github.zjcong.metis.experiments.simple


import com.github.zjcong.metis.EntryPolicy
import com.github.zjcong.metis.execution.RelayExecution
import com.github.zjcong.metis.execution.SimpleExecution
import com.github.zjcong.metis.experiments.benchmark.Schwefel
import com.github.zjcong.metis.samplers.CovarianceMatrixAdaption
import com.github.zjcong.metis.samplers.DifferentialEvolution
import com.github.zjcong.metis.samplers.GeneticAlgorithm
import com.github.zjcong.metis.samplers.ParticleSwampOptimization
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers


fun main() {

    val dimensions = 30
    val population = 30
    val problem = Schwefel(dimensions)
    val alternatingThreshold = 100

    val names = setOf(
        "CMA-ES",
        "Alternating",
    )

    val simpleExperiment = SimpleExperiment<DoubleArray>(names) { name, monitor ->
        when (name) {
            "CMA-ES" -> SimpleExecution(
                name = name,
                problem = problem,
                sampler = CovarianceMatrixAdaption(problem.dimensions, population),
                monitor = monitor,
                entryPolicy = EntryPolicy.CLOSED_BORDER
            )
            "Alternating" -> RelayExecution(
                name = name,
                problem = problem,
                samplers = listOf(
                    CovarianceMatrixAdaption(problem.dimensions),
                    DifferentialEvolution(problem.dimensions),
                    ParticleSwampOptimization(problem.dimensions),
                    GeneticAlgorithm(problem.dimensions),
                ),
                threshold = alternatingThreshold,
                monitor = monitor,
                entryPolicy = EntryPolicy.CLOSED_BORDER
            )
            else -> throw IllegalStateException("Should not happen")
        }
    }

    val results = simpleExperiment.start()

    val chart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("Default vs Alternating Engine on ${dimensions}D Schwefel function (p=$population)")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    results.forEach { (name, history) ->
        val series = chart.addSeries(name, history.map { it.second })
        series.marker = SeriesMarkers.NONE
    }

    SwingWrapper(chart)
        .setTitle("OptimK Experiment Result")
        .displayChart()
}
