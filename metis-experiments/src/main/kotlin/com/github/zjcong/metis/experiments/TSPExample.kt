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

package com.github.zjcong.metis.experiments

import com.github.zjcong.metis.DefaultMonitor
import com.github.zjcong.metis.LogLevel
import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.experiments.benchmark.tsp.DANTZIG42
import com.github.zjcong.metis.experiments.simple.islandsOf
import com.github.zjcong.metis.execution.CompetitiveIslandExecution
import com.github.zjcong.metis.samplers.CovarianceMatrixAdaption
import com.github.zjcong.metis.samplers.DifferentialEvolution
import com.github.zjcong.metis.samplers.GeneticAlgorithm
import com.github.zjcong.metis.samplers.ParticleSwampOptimization
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers


fun main() {
    val problem = DANTZIG42()

    val defaultHistory = mutableListOf<Double>()

    CompetitiveIslandExecution(
        name = "TSP Example",
        problem = problem,
        monitor = object : DefaultMonitor<IntArray>(LogLevel.DEBUG) {
            override fun onIteration(execution: Execution<IntArray>) {
                if (execution.iterations.rem(100) == 0L) defaultHistory.add(execution.bestSolution.fitness)
                super.onIteration(execution)
            }
        },
        islands = islandsOf(
            4, problem, object : DefaultMonitor<IntArray>() {}, listOf(
                CovarianceMatrixAdaption(problem.dimensions),
                GeneticAlgorithm(problem.dimensions),
                ParticleSwampOptimization(problem.dimensions),
                DifferentialEvolution(problem.dimensions),
            )
        ),
    ).apply { optimize() }

    val chart = XYChartBuilder()
        .width(1024)
        .height(700)
        .title("TSP - Default Engine Example")
        .xAxisTitle("Iterations")
        .yAxisTitle("Distance")
        .theme(Styler.ChartTheme.GGPlot2)
        .build()

    chart.addSeries("Competitive Islands", defaultHistory).marker = SeriesMarkers.NONE
    chart.styler.isYAxisLogarithmic = true
    SwingWrapper(chart).displayChart()
}