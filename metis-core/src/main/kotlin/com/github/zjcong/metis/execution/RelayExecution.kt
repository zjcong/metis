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

/**
 * Alternating engine
 */
open class RelayExecution<T>(
    name: String,
    problem: Problem<T>,
    private val samplers: List<Sampler>,
    private val threshold: Int = problem.dimensions * 10,
    monitor: Monitor<T>,
    populationSize: Int = problem.dimensions * 10,
    entryPolicy: EntryPolicy = EntryPolicy.CLOSED_BORDER
) : SimpleExecution<T>(name, problem, monitor, samplers[0], populationSize, entryPolicy) {

    /**
     * Number of iterations of unchanged best fitness
     */
    protected var stagnation: Int = 0

    /**
     * Index of active optimizer in optimizers list
     */
    protected var activeOptimizerIndex: Int = 0

    init {
        require(samplers.isNotEmpty()) { "At least one optimizer must be specified" }
        require(threshold > 0) { "Stagnation threshold must be greater than zero" }
        require(samplers.all { it.dimensions == samplers[0].dimensions }) { "Optimizers must have consistent dimensionality" }
    }

    /**
     * TODO
     *
     */
    override fun updateFitness() {
        val lastBest = bestSolution
        super.updateFitness()
        if (lastBest <= bestSolution) stagnation++
        else stagnation = 0
    }


    protected open fun alternatePolicy() {
        // Change sampler
        if (stagnation > threshold) {
            activeOptimizerIndex++
            sampler = samplers[activeOptimizerIndex.rem(samplers.size)]
            population = sampler.initializeWith(
                population.map { it as EvaluatedIndividual }.asPopulation(), populationSize
            )
            info("Engine relayed to [${sampler.javaClass.simpleName}] with population of [${population.size}]")
            stagnation = 0
            updateFitness()
        }
    }


    override fun nextIteration() {
        alternatePolicy()
        return super.nextIteration()
    }
}