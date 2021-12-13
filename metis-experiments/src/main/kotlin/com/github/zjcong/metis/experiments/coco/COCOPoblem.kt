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

package com.github.zjcong.metis.experiments.coco

import CocoProblem
import com.github.zjcong.metis.*
import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.problem.Goal
import com.github.zjcong.metis.problem.SingleObjectiveProblem
import com.github.zjcong.metis.valueIn

/**
 *
 */
abstract class SingleObjectiveCOCOSingleObjectiveProblem(val cocoProblem: CocoProblem) :
    SingleObjectiveProblem<DoubleArray>() {

    override val dimensions: Int
        get() = cocoProblem.dimension

    override val goal: Goal = Goal.Minimize

    fun getMonitor(): Monitor<DoubleArray> = COCOMonitor(
        cocoProblem
    )

    override fun objective(solution: DoubleArray): Double {
        return cocoProblem.evaluateFunction(solution)[0]
    }

    override fun shouldStop(execution: Execution<DoubleArray>): Boolean {
        return (System.currentTimeMillis() - execution.startTime >= 15_000 || cocoProblem.isFinalTargetHit)
    }

    private class COCOMonitor(val p: CocoProblem) : DefaultMonitor<DoubleArray>(LogLevel.WARN) {
        override fun onIteration(execution: Execution<DoubleArray>) {
            if (p.isFinalTargetHit) print("|")
            super.onIteration(execution)
        }

    }
}


/**
 * Continuous problem
 */
internal class ContinuousCOCOSingleObjectiveProblem(cocoProblem: CocoProblem) :
    SingleObjectiveCOCOSingleObjectiveProblem(cocoProblem) {

    override fun decode(individual: Individual): DoubleArray {
        val keys = individual.keys
        return keys.indices.map { keys[it].valueIn((-5.0).rangeTo(5.0)) }.toDoubleArray()
    }

    override fun isFeasible(solution: DoubleArray): Boolean = true


}


/**
 * Mix Int problem
 */
internal class MixIntCOCOSingleObjectiveProblem(
    cocoProblem: CocoProblem,
) : SingleObjectiveCOCOSingleObjectiveProblem(cocoProblem) {

    override fun decode(individual: Individual): DoubleArray {
        val keys = individual.keys
        require(keys.size.rem(5) == 0)

        val solution = DoubleArray(keys.size)
        val p = keys.size / 5

        (0 until 5).forEach { pi ->

            (0 until p).forEach {
                val si = pi * p + it
                val v = when (pi) {
                    0 -> keys[si].valueIn(0..1).toDouble()
                    1 -> keys[si].valueIn(0..3).toDouble()
                    2 -> keys[si].valueIn(0..7).toDouble()
                    3 -> keys[si].valueIn(0..15).toDouble()
                    4 -> keys[si].valueIn((-5.0).rangeTo(5.0))
                    else -> {
                        throw RuntimeException("This should not happen")
                    }
                }
                solution[si] = v
            }
        }
        return solution
    }

    override fun isFeasible(solution: DoubleArray): Boolean = true


}


