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

package com.github.zjcong.metis

import com.github.zjcong.metis.execution.Execution
import java.io.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

/**
 * Monitor Interface
 *
 * @param T
 */
interface Monitor<T> : Serializable {
    fun onStart(execution: Execution<T>)
    fun onIteration(execution: Execution<T>)
    fun onTerminate(execution: Execution<T>)

    fun log(level: LogLevel, execution: Execution<T>, msg: String)
}

/**
 * This class is the default monitor
 */
abstract class DefaultMonitor<T>(private val level: LogLevel = LogLevel.INFO) : Monitor<T> {

    override fun onStart(execution: Execution<T>) {
        val start = LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.startTime), ZoneId.systemDefault())
        log(LogLevel.INFO, execution, "Optimization started at $start")
    }

    override fun onIteration(execution: Execution<T>) {
        val msg = StringBuilder()
            .append("Iteration [${execution.iterations}] finished.")
            .append("Best fitness: [${execution.bestSolution.fitness}]").toString()
        log(LogLevel.INFO, execution, msg)
    }

    override fun onTerminate(execution: Execution<T>) {
        val duration = System.currentTimeMillis() - execution.startTime
        val now = LocalDateTime.now()
        val msg = StringBuilder()
            .append("Optimization terminated after [$duration]ms at [$now],")
            .append("with best fitness: [${execution.bestSolution.fitness}]").toString()
        log(LogLevel.INFO, execution, msg)
    }

    override fun log(level: LogLevel, execution: Execution<T>, msg: String) {
        if (level < this.level) {
            return
        }
        println("${level.name} [${execution.name}]:  $msg")
    }
}