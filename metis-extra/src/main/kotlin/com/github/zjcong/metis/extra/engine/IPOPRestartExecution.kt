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

package com.github.zjcong.metis.extra.engine

import com.github.zjcong.metis.*
import com.github.zjcong.metis.execution.RestartExecution
import com.github.zjcong.metis.problem.Problem
import com.github.zjcong.metis.samplers.Sampler
import kotlin.math.roundToInt

class IPOPRestartExecution<T>(
    name: String,
    problem: Problem<T>,
    sampler: Sampler,
    monitor: Monitor<T>,
    threshold: Int = problem.dimensions * 10,
    private val increaseFactor: Double = 1.5,
    populationSize: Int = problem.dimensions * 3,
    entryPolicy: EntryPolicy = EntryPolicy.CLOSED_BORDER
) : RestartExecution<T>(name, problem, sampler, monitor, threshold, populationSize, entryPolicy) {

    override fun restartPolicy() {
        if (stagnation > threshold) {
            populationSize = (populationSize * increaseFactor).roundToInt()
            info("Engine restart at iteration [$iterations]")
            population = sampler.initializeWith(
                Population(mutableListOf(population.bestIndividual())), populationSize
            )
            stagnation = 0
            updateFitness()
        }
    }
}