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
import com.github.zjcong.metis.samplers.Sampler
import kotlin.math.sign


open class DefaultExecution<T>(
    override val name: String,
    override val problem: Problem<T>,
    override val monitor: Monitor<T>,
    protected var sampler: Sampler,
    override var populationSize: Int = problem.dimensions * 10,
    protected val entryPolicy: EntryPolicy = EntryPolicy.CLOSED_BORDER
) : Execution<T>() {

    var population: Population = Population()

    /**
     * Update fitness
     */
    override fun updateFitness() {
        if (population.isEmpty()) population = sampler.initialize(populationSize)
        population = evaluatePopulation(population)
    }


    /**
     * Generate samples for next iteration
     */
    override fun nextIteration() {
        iterations++
        population = sampler.iterate(population, entryPolicy)
        populationSize = populationSize.sign
    }

    /**
     * TODO
     *
     * @param immigrant
     */
    override fun arrival(immigrant: EvaluatedIndividual) = sampler.arrival(immigrant)

    /**
     * TODO
     *
     * @param population
     * @param populationSize
     */
    override fun reinitializeWith(population: Population, populationSize: Int) {
        this.populationSize = populationSize
        this.population = sampler.initializeWith(population, populationSize)
    }

    /**
     * TODO
     *
     * @param populationSize
     */
    override fun reinitializeWith(populationSize: Int) {
        this.populationSize = populationSize
        this.population = sampler.initializeWith(population, populationSize)
    }

}


