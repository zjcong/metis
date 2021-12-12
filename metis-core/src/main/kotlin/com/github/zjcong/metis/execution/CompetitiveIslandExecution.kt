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

@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.zjcong.metis.execution

import com.github.zjcong.metis.*
import kotlin.math.roundToInt

/**
 * Competitive Island
 *
 * @param T
 * @property name
 * @property problem
 * @property monitor
 * @property populationSize
 * @property adjustmentFrequency
 * @constructor
 * TODO
 *
 * @param islands
 * @param migrationFrequency
 * @param seed
 */
open class CompetitiveIslandExecution<T>(
    override val name: String,
    override val problem: Problem<T>,
    islands: List<Execution<T>>,
    override val monitor: Monitor<T>,
    override var populationSize: Int = problem.dimensions * 10,
    migrationFrequency: Int = 1,
    val adjustmentFrequency: Int = problem.dimensions * 200,
    seed: Int = 0
) : IslandExecution<T>(name, problem, islands, migrationFrequency, monitor, seed) {

    private val bestRecord: MutableMap<Execution<T>, Double> = islands.associateWith { Double.MAX_VALUE }.toMutableMap()
    private val totalPopulationSize = islands.sumOf { it.populationSize }

    open fun adjustmentPolicy() {
        if (iterations.rem(adjustmentFrequency) != 0L) return
        val best = islands.maxByOrNull { it.bestSolution.fitness - bestRecord[it]!! }!!
        val bestPopulation = (totalPopulationSize * 0.80).roundToInt().coerceAtMost(totalPopulationSize)
        if (best.populationSize == bestPopulation) return
        best.reinitializeWith(bestPopulation)
        info("Population size of [${best.name}] increased to $bestPopulation")
        islands.forEach {
            if (it != best) {
                it.reinitializeWith((it.populationSize * 0.20 / (totalPopulationSize - 1)).roundToInt())
                info("Population size of [${it.name}] decreased to ${(it.populationSize * 0.5 / (totalPopulationSize - 1)).roundToInt()}")
            }
        }
        islands.forEach { it.updateFitness() }
        updateBestRecord()
    }

    override fun nextIteration() {
        iterations++
        if (iterations == 1L)
            updateBestRecord()

        // migrate
        migratePolicy()
        //adjust population size
        adjustmentPolicy()
        islands.forEach { it.nextIteration() }
        debug("Iteration [$iterations] finished, fitness: [${bestSolution.fitness}]")
    }


    private fun updateBestRecord() {
        islands.forEach { bestRecord[it] = it.bestSolution.fitness }
    }


}
