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

package com.github.zjcong.metis.experiments.simple

import com.github.zjcong.metis.DefaultMonitor
import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.LogLevel
import com.github.zjcong.metis.Monitor


/**
 *
 */
private class ExperimentMonitor<T> : DefaultMonitor<T>(LogLevel.INFO) {

    val history: MutableList<Pair<Long, Double>> = mutableListOf()

    override fun onIteration(execution: Execution<T>) {
        if (execution.iterations.rem(100L) == 0L || execution.iterations == 1L)
            history.add(
                Pair(
                    execution.evaluations, execution.bestSolution.fitness
                )
            )
        super.onIteration(execution)
    }

}


/**
 *
 */
class SimpleExperiment<T>(
    names: Set<String>, enginesOf: (name: String, monitor: Monitor<T>) -> Execution<T>
) {

    private val engines: Map<String, Execution<T>>

    init {
        require(names.isNotEmpty())
        engines = names.associateWith { enginesOf(it, ExperimentMonitor()) }
    }

    fun start(): Map<String, MutableList<Pair<Long, Double>>> {

        val results = engines.map { (t, u) ->
            u.optimize()
            Pair(t, (u.monitor as ExperimentMonitor).history)
        }.toMap()

        return results
    }
}

