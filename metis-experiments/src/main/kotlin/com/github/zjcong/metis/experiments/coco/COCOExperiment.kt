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

@file:Suppress("DuplicatedCode")

package com.github.zjcong.metis.experiments.coco

import Timing
import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.EntryPolicy
import com.github.zjcong.metis.LogLevel
import com.github.zjcong.metis.execution.RelayExecution
import com.github.zjcong.metis.execution.IslandExecution
import com.github.zjcong.metis.execution.RestartExecution
import com.github.zjcong.metis.extra.engine.CharityIsland
import com.github.zjcong.metis.execution.CompetitiveIslandExecution
import com.github.zjcong.metis.samplers.CovarianceMatrixAdaption
import com.github.zjcong.metis.samplers.DifferentialEvolution
import com.github.zjcong.metis.samplers.GeneticAlgorithm
import com.github.zjcong.metis.samplers.ParticleSwampOptimization
import java.lang.Integer.max

const val restartMultiplier = 28
val LOGLEVEL = LogLevel.WARN

/**
 *
 */
@Suppress("unused", "SameParameterValue")
private fun engineOf(
    name: String,
    problem: SingleObjectiveCOCOSingleObjectiveProblem,
    population: Int,
    seed: Int,
): Execution<DoubleArray> {
    require(population > 25) { "population too small" }
    @Suppress("SpellCheckingInspection")
    return when (name) {
        "CMAES" -> RestartExecution(
            name = name,
            monitor = problem.getMonitor(),
            sampler = CovarianceMatrixAdaption(problem.dimensions, seed = seed),
            problem = problem,
            threshold = (problem.dimensions * restartMultiplier),
            populationSize = population,
        )
        "DE" -> RestartExecution(
            name = name,
            monitor = problem.getMonitor(),
            sampler = DifferentialEvolution(problem.dimensions, seed = seed),
            problem = problem,
            threshold = (problem.dimensions * restartMultiplier),
            populationSize = population,

            )
        "PSO" -> RestartExecution(
            name = name,
            monitor = problem.getMonitor(),
            sampler = ParticleSwampOptimization(problem.dimensions, seed = seed),
            problem = problem,
            threshold = (problem.dimensions * restartMultiplier),
            populationSize = population,

            )
        "GA" -> RestartExecution(
            name = name,
            monitor = problem.getMonitor(),
            sampler = GeneticAlgorithm(problem.dimensions, seed = seed),
            problem = problem,
            threshold = (problem.dimensions * restartMultiplier),
            populationSize = population,

            )
        "ISLAND" -> {
            val islandCount = 4
            val restartIslands: List<Execution<DoubleArray>> = listOf(
                //CovarianceMatrixAdaption(problem.dimensions, population, seed = seed),
                CovarianceMatrixAdaption(problem.dimensions, seed = seed),
                GeneticAlgorithm(problem.dimensions, seed = seed),
                ParticleSwampOptimization(problem.dimensions, seed = seed),
                DifferentialEvolution(problem.dimensions, seed = seed)
            ).map {
                RestartExecution(
                    name = "Island-${it.javaClass.simpleName}",
                    problem = problem,
                    sampler = it,
                    monitor = problem.getMonitor(),
                    threshold = (problem.dimensions * restartMultiplier),
                    populationSize = population / islandCount,
                    entryPolicy = EntryPolicy.LAST_TO_RANDOM
                )
            }

            IslandExecution(
                name = "Island",
                problem = problem,
                monitor = problem.getMonitor(),
                islands = restartIslands,
                seed = seed
            )
        }
        "COMPETITIVE" -> {
            val islandCount = 4
            val restartIslands: List<Execution<DoubleArray>> = listOf(
                //CovarianceMatrixAdaption(problem.dimensions, population, seed = seed),
                CovarianceMatrixAdaption(problem.dimensions, seed = seed),
                GeneticAlgorithm(problem.dimensions, seed = seed),
                ParticleSwampOptimization(problem.dimensions, seed = seed),
                DifferentialEvolution(problem.dimensions, seed = seed)
            ).map {
                RestartExecution(
                    name = "COMPETITIVE-${it.javaClass.simpleName}",
                    problem = problem,
                    sampler = it,
                    monitor = problem.getMonitor(),
                    threshold = (problem.dimensions * restartMultiplier),
                    populationSize = population / islandCount,
                    entryPolicy = EntryPolicy.LAST_TO_RANDOM
                )
            }
            CompetitiveIslandExecution(
                name = "COMPETITIVE",
                problem = problem,
                monitor = problem.getMonitor(),
                islands = restartIslands,
                adjustmentFrequency = problem.dimensions * 100,
                seed = seed
            )
        }
        "COMPETITIVE_EQUAL_POP" -> {
            val islandCount = 4
            val restartIslands: List<Execution<DoubleArray>> = listOf(
                //CovarianceMatrixAdaption(problem.dimensions, population, seed = seed),
                CovarianceMatrixAdaption(problem.dimensions, seed = seed),
                GeneticAlgorithm(problem.dimensions, seed = seed),
                ParticleSwampOptimization(problem.dimensions, seed = seed),
                DifferentialEvolution(problem.dimensions, seed = seed)
            ).map {
                RestartExecution(
                    name = "COMPETITIVE-${it.javaClass.simpleName}",
                    problem = problem,
                    sampler = it,
                    monitor = problem.getMonitor(),
                    threshold = (problem.dimensions * restartMultiplier),
                    populationSize = population / islandCount,
                    entryPolicy = EntryPolicy.LAST_TO_RANDOM
                )
            }
            CompetitiveIslandExecution(
                name = "COMPETITIVE",
                problem = problem,
                monitor = problem.getMonitor(),
                islands = restartIslands,
                adjustmentFrequency = problem.dimensions * 100,
                seed = seed
            )
        }
        "CHARITY" -> {
            val islandCount = 4
            val restartIslands: List<Execution<DoubleArray>> = listOf(
                //CovarianceMatrixAdaption(problem.dimensions, population, seed = seed),
                CovarianceMatrixAdaption(problem.dimensions, seed = seed),
                GeneticAlgorithm(problem.dimensions, seed = seed),
                ParticleSwampOptimization(problem.dimensions, seed = seed),
                DifferentialEvolution(problem.dimensions, seed = seed)
            ).map {
                RestartExecution(
                    name = "BIPI-${it.javaClass.simpleName}",
                    problem = problem,
                    sampler = it,
                    monitor = problem.getMonitor(),
                    threshold = (problem.dimensions * restartMultiplier),
                    populationSize = population / islandCount,
                    entryPolicy = EntryPolicy.LAST_TO_RANDOM
                )
            }
            CharityIsland(
                name = "BIPI",
                problem = problem,
                monitor = problem.getMonitor(),
                islands = restartIslands,
                seed = seed
            )
        }
        "RELAY" -> {
            val samplers = listOf(
                CovarianceMatrixAdaption(problem.dimensions, seed),
                GeneticAlgorithm(problem.dimensions, seed = seed),
                ParticleSwampOptimization(problem.dimensions, seed = seed),
                DifferentialEvolution(problem.dimensions, seed = seed)
            )

            RelayExecution(
                name = "Relay",
                problem = problem,
                samplers = samplers,
                monitor = problem.getMonitor(),
                threshold = (problem.dimensions),
                populationSize = population
            )
        }
        else -> throw IllegalArgumentException("Unknown sampler: $name")
    }
}

/**
 *
 */
fun runExperiment(benchmark: COCOBenchmark) {

    var problem: SingleObjectiveCOCOSingleObjectiveProblem? = benchmark.nextProblem

    val timing = Timing()

    while (problem != null) {

        val population = max(problem.dimensions * 10, 40)
        val engine = engineOf(benchmark.algorithmName, problem, population, 0x01)

        engine.optimize()

        timing.timeProblem(problem.cocoProblem)
        problem = benchmark.nextProblem
    }

    timing.output()
}

/**
 *
 */
fun main(args: Array<String>) {
    require(args.size > 1)
    require(args[0] in listOf("bbob", "bbob-mixint"))
    require(
        args[1].uppercase() in listOf(
            "CMAES",
            "DE",
            "PSO",
            "GA",
            "ISLAND",
            "RELAY",
            "COMPETITIVE",
            "COMPETITIVE_EQUAL_POP",
            "CHARITY"
        )
    )


    val benchmark = when (args[0]) {
        "bbob" -> BBOBBenchmark(
            args[1].uppercase(),
            "Metis Implementation of ${args[1].uppercase()}",
            "dimensions: ${if (args.size > 2) args[2] else "2,3,5,10,20,40"}",
            1_000L
        )
        "bbob-mixint" -> BBOBBMixIntBenchmark(
            args[1].uppercase(),
            "Metis Implementation of ${args[1].uppercase()}",
            "dimensions: ${if (args.size > 2) args[2] else "5,10,20,40,80,160"}",
            1_000L
        )
        else -> throw IllegalArgumentException()
    }

    runExperiment(benchmark)
}
