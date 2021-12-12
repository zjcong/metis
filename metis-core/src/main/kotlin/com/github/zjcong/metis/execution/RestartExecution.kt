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
 * Restart Engine
 */
open class RestartExecution<T>(
    override val name: String,
    problem: Problem<T>,
    sampler: Sampler,
    monitor: Monitor<T>,
    protected val threshold: Int = problem.dimensions * 10,
    populationSize: Int = problem.dimensions * 10,
    entryPolicy: EntryPolicy = EntryPolicy.CLOSED_BORDER
) : SimpleExecution<T>(name, problem, monitor, sampler, populationSize, entryPolicy) {

    protected var stagnation: Int = 0

    override fun updateFitness() {
        val lastBest = bestSolution
        super.updateFitness()
        if (lastBest <= bestSolution) stagnation++
        else stagnation = 0
    }

    protected open fun restartPolicy() {
        if (stagnation > threshold) {
            info("Engine restart at iteration [$iterations]")
            population = sampler.initializeWith(
                Population(mutableListOf(population.bestIndividual())),
                populationSize
            )
            stagnation = 0
            updateFitness()
        }
    }

    override fun nextIteration() {
        restartPolicy()
        return super.nextIteration()
    }
}