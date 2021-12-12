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
@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.zjcong.metis.execution

import com.github.zjcong.metis.*
import java.io.*

/**
 * Engine
 */
abstract class Execution<T> : Serializable {

    abstract val name: String

    /**
     * The problem
     */
    abstract val problem: Problem<T>


    abstract var populationSize: Int

    /**
     * Monitor
     */
    abstract val monitor: Monitor<T>

    /**
     * Best solution of the current generation
     */
    var bestSolution: EvaluatedIndividual = EvaluatedIndividual.DUMMY
        protected set

    /**
     * Number of evaluations
     */
    var evaluations: Long = 0L
        protected set

    /**
     * Start time
     */
    var startTime: Long = 0
        protected set

    /**
     * Number of iterations
     */
    var iterations: Long = 0
        protected set

    /**
     * Wrapper of log
     */
    protected fun log(level: LogLevel, msg: String): Unit = monitor.log(level, this, msg)
    protected fun debug(msg: String): Unit = log(LogLevel.DEBUG, msg)
    protected fun info(msg: String): Unit = log(LogLevel.INFO, msg)
    protected fun warn(msg: String): Unit = log(LogLevel.WARN, msg)
    protected fun error(msg: String): Unit = log(LogLevel.ERROR, msg)


    /**
     *
     */
    abstract fun updateFitness()
    abstract fun nextIteration()
    abstract fun arrival(immigrant: EvaluatedIndividual)

    abstract fun reinitializeWith(populationSize: Int)
    abstract fun reinitializeWith(population: Population, populationSize: Int)

    /**
     * Evaluate a batch of solutions
     */
    fun evaluatePopulation(population: Population): Population {
        evaluations += population.size
        if (population.size <= 0) println(this.name)
        val fitnessValues = problem(population)
        fitnessValues.indices.forEach { si ->
            if (fitnessValues[si].isNaN()) throw RuntimeException("Solution: ${problem.decode(population[si])} yields NaN value")
        }
        val evaluated = population.associateWithFitness(fitnessValues)
        val cBest = evaluated.bestIndividual()
        if (cBest <= bestSolution)
            bestSolution = cBest

        return evaluated
    }

    /**
     * Start optimization
     */
    open fun optimize(): T? {
        this.startTime = System.currentTimeMillis()
        monitor.onStart(this)
        do {
            updateFitness()
            nextIteration()
            monitor.onIteration(this)
        } while (!problem.shouldStop(this))
        monitor.onTerminate(this)
        return if (bestSolution.fitness != Double.MAX_VALUE) {
            info("Engine terminated with best fitness [${bestSolution.fitness}] after [${System.currentTimeMillis() - startTime}]ms")
            problem.decode(bestSolution)
        } else {
            warn("Engine failed to find any feasible solution")
            null
        }
    }

    /**
     * Serialize this engine to file
     */
    fun suspendTo(file: File) {
        val fos = FileOutputStream(file)
        val oos = ObjectOutputStream(fos)
        oos.writeObject(this)
        oos.close()
        fos.close()
        info("Engine suspended to [${file.name}]")
    }

    /**
     * Companion functions
     */
    companion object {

        /**
         * Deserialize from file
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> resumeFrom(f: File): Execution<T> {
            val fis = FileInputStream(f)
            val ois = ObjectInputStream(fis)
            val execution = ois.readObject() as Execution<*>
            fis.close()
            ois.close()
            return execution as Execution<T>
        }
    }

}

