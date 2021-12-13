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

package com.github.zjcong.metis.problem

import com.github.zjcong.metis.Population


/**
 * Problem interface
 *
 * @param T Type of actual solution
 */
abstract class SingleObjectiveProblem<T> : Problem<T> {

    /**
     * Goal (maximize or minimize)
     */
    abstract val goal: Goal

    /**
     * Objective function
     *
     * @param solution
     * @return fitness value
     */
    protected abstract fun objective(solution: T): Double

    /**
     * Evaluate a population
     *
     * @param population
     * @return
     */
    override operator fun invoke(population: Population): DoubleArray {
        return population.map { individual ->
            if (individual.any { it !in 0.0..1.0 }) Double.MAX_VALUE
            else {
                val decoded = decode(individual)
                if (!isFeasible(decoded)) Double.MAX_VALUE
                else goal * objective(decoded)
            }
        }.toDoubleArray()
    }
}


/**
 * TODO
 *
 * @param T
 */
abstract class ParallelSingleObjectiveProblem<T> : SingleObjectiveProblem<T>() {
    override fun invoke(population: Population): DoubleArray {
        return population.toList().parallelStream().mapToDouble { individual ->
            if (individual.any { it !in 0.0..1.0 }) Double.MAX_VALUE
            else {
                val decoded = decode(individual)
                if (!isFeasible(decoded)) Double.MAX_VALUE
                else objective(decoded)
            }
        }.toArray()
    }
}



