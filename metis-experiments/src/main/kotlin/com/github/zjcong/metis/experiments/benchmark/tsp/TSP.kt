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

package com.github.zjcong.metis.experiments.benchmark.tsp

import com.github.zjcong.metis.problem.Goal
import com.github.zjcong.metis.Individual
import com.github.zjcong.metis.problem.SingleObjectiveProblem
import com.github.zjcong.metis.execution.Execution


abstract class TSP(override val dimensions: Int) : SingleObjectiveProblem<IntArray>() {

    override val goal: Goal = Goal.Minimize

    abstract val distanceMatrix: Array<IntArray>

    abstract val globalOptima: Long

    override fun objective(solution: IntArray): Double {
        return solution.indices.sumOf {
            val from = solution[it]
            val to = if (it == (solution.size - 1)) solution[0] else solution[it + 1]
            distanceMatrix[from][to]
        }.toDouble() - this.globalOptima.toDouble()
    }

    override fun decode(individual: Individual): IntArray {
        return individual.withIndex().sortedBy { it.value }.map { it.index }.toIntArray()
    }

    override fun shouldStop(execution: Execution<IntArray>): Boolean {
        return (execution.bestSolution.fitness <= 0.0 ||
                System.currentTimeMillis() - execution.startTime >= 60_000 //|| engine.iterations >= 30_000
                )
    }

    override fun isFeasible(solution: IntArray): Boolean = true
}