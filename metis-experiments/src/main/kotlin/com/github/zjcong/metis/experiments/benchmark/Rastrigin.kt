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

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

class Rastrigin(d: Int) : ContinuousBenchmarkSingleObjectiveProblem(d) {

    private val A = 10
    override val lowerBound: Double = -5.12
    override val upperBound: Double = 5.12
    override val globalOptima: Double = 0.0

    override fun objective(solution: DoubleArray): Double {

        var sum = 0.0
        for (i in solution.indices) {
            sum += solution[i].pow(2) - A * cos(2.0 * PI * solution[i])
        }
        return A * dimensions + sum
    }
}