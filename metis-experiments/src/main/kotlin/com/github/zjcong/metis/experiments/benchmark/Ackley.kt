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

import kotlin.math.*

open class Ackley(d: Int) : ContinuousBenchmarkProblem(d) {

    override val lowerBound: Double = -32.70
    override val upperBound: Double = 32.70
    override val globalOptima: Double = 0.0

    private var a = 20.0
    private var b = 0.2
    private var c = 2.0 * PI

    override fun objective(solution: DoubleArray): Double {
        var sum1 = 0.0
        var sum2 = 0.0
        for (i in 0 until dimensions) {
            sum1 += solution[i].pow(2.0)
            sum2 += cos(c * solution[i])
        }
        return -20.0 * exp(-0.2 * sqrt(sum1 / dimensions.toDouble())) + 20.0 - exp(sum2 / dimensions.toDouble()) + exp(1.0)
    }

}

