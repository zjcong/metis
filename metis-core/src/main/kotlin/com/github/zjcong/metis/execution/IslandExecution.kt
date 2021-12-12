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

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.github.zjcong.metis.execution

import com.github.zjcong.metis.*
import kotlin.random.Random


/**
 * Island Model Engine
 */
open class IslandExecution<T>(
    override val name: String,
    override val problem: Problem<T>,
    protected val islands: List<Execution<T>>,
    protected val migrationFrequency: Int = 1,
    override val monitor: Monitor<T>,
    seed: Int = 0
) : Execution<T>() {

    protected val rng: Random = Random(seed)

    override var populationSize: Int = islands.sumOf { it.populationSize }

    open fun migratePolicy() {
        if (iterations.rem(migrationFrequency) != 0L || iterations < migrationFrequency) return
        val destination = islands[rng.nextInt(islands.size)]
        val origin = islands[rng.nextInt(islands.size)]
        if (destination != origin) destination.arrival(origin.bestSolution)
        debug("Immigrant from [${origin.name}] to [${destination.name}]")
    }

    override fun updateFitness() {
        // Update number of evaluations
        evaluations = islands.sumOf { it.evaluations }
        // Evaluate islands
        islands.forEach { it.updateFitness() }
        // Update best individual
        val min = islands.minByOrNull { it.bestSolution.fitness }!!
        if (min.bestSolution <= bestSolution || bestSolution.fitness == Double.MAX_VALUE) {
            bestSolution = min.bestSolution
        }
    }

    override fun nextIteration() {
        iterations++
        // migrate
        migratePolicy()
        islands.forEach { it.nextIteration() }
        populationSize = islands.sumOf { it.populationSize }
    }


    override fun arrival(immigrant: EvaluatedIndividual) {
        islands.forEach { it.arrival(immigrant) }
    }

    override fun reinitializeWith(population: Population, populationSize: Int) {
        islands.forEach { it.reinitializeWith(population, populationSize) }
    }

    override fun reinitializeWith(populationSize: Int) {
        islands.forEach { it.reinitializeWith(populationSize) }
    }

}
