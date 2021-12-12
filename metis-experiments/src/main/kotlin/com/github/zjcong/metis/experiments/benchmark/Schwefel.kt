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
import kotlin.math.sin
import kotlin.math.sqrt


class Schwefel(d: Int) : ContinuousBenchmarkProblem(d) {

    override val lowerBound: Double = -500.0
    override val upperBound: Double = 500.0

    override val globalOptima: Double = d * 0.4971429158184719

    override fun objective(solution: DoubleArray): Double {
        var sum = 0.0
        for (i in 0 until dimensions) {
            sum += -solution[i] * sin(sqrt(abs(solution[i])))
        }
        return sum + dimensions * 4.18982887272434686131e+02
    }
}