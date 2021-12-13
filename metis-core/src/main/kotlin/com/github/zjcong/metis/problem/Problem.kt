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

import com.github.zjcong.metis.Individual
import com.github.zjcong.metis.Population
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


interface Problem<T> : Serializable {
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
     * TODO
     *
     * @param population
     * @return
     */
    operator fun invoke(population: Population): DoubleArray
}