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

package com.github.zjcong.metis

import com.github.zjcong.metis.execution.Execution
import java.io.Serializable


/**
 * Goal type
 *
 * @property value
 */
enum class Goal(private val value: Int) {
    Maximize(-1), Minimize(1);

    operator fun times(value: Double): Double = value * this.value
}

/**
 * Problem interface
 *
 * @param T Type of actual solution
 */
interface Problem<T> : Serializable {

    /**
     * Goal (maximize or minimize)
     */
    val goal: Goal

    /**
     * Dimensions
     */
    val dimensions: Int

    /**
     * Decode random keys into actual solution
     *
     * @param individual
     * @return
     */
    fun decode(individual: Individual): T

    /**
     * Objective function
     *
     * @param solution
     * @return fitness value
     */
    fun objective(solution: T): Double

    /**
     * If a solution is feasible
     *
     * @param solution
     * @return
     */
    fun isFeasible(solution: T): Boolean

    /**
     * Stop condition
     *
     * @param execution
     * @return
     */
    fun shouldStop(execution: Execution<T>): Boolean

    /**
     * Evaluate a population
     *
     * @param population
     * @return
     */
    operator fun invoke(population: Population): DoubleArray {
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
interface ParallelProblem<T> : Problem<T> {
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






