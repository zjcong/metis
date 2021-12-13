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

import kotlin.math.pow

class Rosenbrock(d: Int) : ContinuousBenchmarkSingleObjectiveProblem(d) {

    override val lowerBound: Double = -5.0
    override val upperBound: Double = 10.0
    override val globalOptima: Double = 0.0

    override fun objective(solution: DoubleArray): Double {
        val X = solution
        var sum = 0.0
        for (i in 1 until solution.size - 1) {
            sum += 100.0 * (X[i + 1] - X[i].pow(2)).pow(2) + (1 - X[i]).pow(2)
        }
        return sum
    }
}