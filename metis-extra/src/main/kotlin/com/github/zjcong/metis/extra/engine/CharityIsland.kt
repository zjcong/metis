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

import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.Monitor
import com.github.zjcong.metis.problem.SingleObjectiveProblem
import com.github.zjcong.metis.execution.IslandExecution
import com.github.zjcong.metis.problem.Problem

open class CharityIsland<T>(
    override val name: String,
    override val problem: Problem<T>,
    islands: List<Execution<T>>,
    override val monitor: Monitor<T>,
    override var populationSize: Int = problem.dimensions * 10,
    migrationFrequency: Int = 1,
    seed: Int = 0
) : IslandExecution<T>(name, problem, islands, migrationFrequency, monitor, seed) {

    override fun migratePolicy() {
        if (iterations.rem(migrationFrequency) != 0L || iterations < migrationFrequency) return
        val destination = islands.maxByOrNull { it.bestSolution.fitness }!!
        val origin = islands[rng.nextInt(islands.size)]
        if (destination != origin) destination.arrival(origin.bestSolution)
    }
}