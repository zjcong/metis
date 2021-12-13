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

package com.github.zjcong.metis.experiments.benchmark


import com.github.zjcong.metis.*
import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.problem.Goal
import com.github.zjcong.metis.problem.ParallelSingleObjectiveProblem
import com.github.zjcong.metis.valueIn

import kotlin.math.abs


sealed class ContinuousBenchmarkSingleObjectiveProblem(
    final override val dimensions: Int
) : ParallelSingleObjectiveProblem<DoubleArray>() {

    override val goal: Goal = Goal.Minimize

    abstract val upperBound: Double
    abstract val lowerBound: Double

    abstract val globalOptima: Double

    override operator fun invoke(population: Population): DoubleArray {
        val objValues = super.invoke(population)
        return DoubleArray(objValues.size) { abs(objValues[it] - globalOptima) }
    }

    override fun decode(individual: Individual): DoubleArray {
        return DoubleArray(dimensions) { i ->
            individual[i].valueIn(lowerBound.rangeTo(upperBound))
        }
    }


    override fun shouldStop(execution: Execution<DoubleArray>): Boolean {
        return (execution.bestSolution.fitness <= 1E-9 ||
                System.currentTimeMillis() - execution.startTime >= 5_000 ||
                execution.iterations >= 5000
                )
    }

    override fun isFeasible(solution: DoubleArray): Boolean = true
}

