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

package com.github.zjcong.metis.experiments


import com.github.zjcong.metis.DefaultMonitor
import com.github.zjcong.metis.execution.Execution
import com.github.zjcong.metis.EntryPolicy
import com.github.zjcong.metis.LogLevel
import com.github.zjcong.metis.execution.SimpleExecution
import com.github.zjcong.metis.experiments.benchmark.SumOfDifferentPowers
import com.github.zjcong.metis.samplers.DifferentialEvolution
import java.io.File


fun main() {


    val engine = SimpleExecution(
        name = "Default DE",
        problem = SumOfDifferentPowers(100),
        sampler = DifferentialEvolution(
            dimensions = 100,
        ),
        monitor = object : DefaultMonitor<DoubleArray>(LogLevel.INFO) {

            override fun onIteration(execution: Execution<DoubleArray>) {
                //Save state every 5 iterations
                if (execution.iterations.rem(5L) == 0L) execution.suspendTo(File("suspended_engine.bin"))
            }
        },
        entryPolicy = EntryPolicy.CLOSED_BORDER
    )

    val result1 = engine.optimize()

    // resume from the saved state
    val resumedExecution =
        Execution.resumeFrom<DoubleArray>(File("suspended_engine.bin"))
    val result2 = resumedExecution.optimize()

    assert(result1.contentEquals(result2))
}
