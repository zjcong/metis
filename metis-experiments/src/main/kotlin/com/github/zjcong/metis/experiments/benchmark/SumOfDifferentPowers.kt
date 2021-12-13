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

import kotlin.math.abs
import kotlin.math.pow

class SumOfDifferentPowers(d: Int) : ContinuousBenchmarkSingleObjectiveProblem(d) {

    override val lowerBound: Double = -1.0
    override val upperBound: Double = 1.0

    override val globalOptima: Double = 0.0

    override fun objective(solution: DoubleArray): Double {
        var sum = 0.0
        for (i in solution.indices) {
            sum += abs(solution[i]).pow(i + 2)
        }
        return sum
    }
}

