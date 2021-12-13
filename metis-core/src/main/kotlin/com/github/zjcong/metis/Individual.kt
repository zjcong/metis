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
 *
 */

package com.github.zjcong.metis

import java.io.Serializable


/**
 * An individual, thin wrapper around a DoubleArray
 *
 * @property keys
 */
open class Individual(val keys: DoubleArray) : Iterable<Double>, Serializable {

    val dimensions: Int by lazy { keys.size }

    constructor(dimensions: Int, init: (Int) -> Double) : this(DoubleArray(dimensions, init))

    internal constructor() : this(doubleArrayOf())

    operator fun get(index: Int): Double = keys[index]

    //Scalar operations
    operator fun times(scalar: Double): Individual = Individual(dimensions) { keys[it] * scalar }
    operator fun plus(other: Individual): Individual = Individual(dimensions) { keys[it] + other[it] }
    operator fun plus(other: DoubleArray): Individual = Individual(dimensions) { keys[it] + other[it] }
    operator fun minus(other: Individual): Individual = Individual(dimensions) { keys[it] - other[it] }
    operator fun minus(other: DoubleArray): Individual = Individual(dimensions) { keys[it] - other[it] }

    fun asEvaluated(fitness: Double): EvaluatedIndividual = EvaluatedIndividual(keys, fitness)

    override fun iterator(): Iterator<Double> = keys.iterator()

    companion object {
        val DUMMY = Individual()
    }
}

/**
 * TODO
 *
 * @return
 */
fun DoubleArray.asIndividual(): Individual = Individual(this)


/**
 * An evaluated individual, basically an individual with a fitness value attached
 *
 * @property keys
 * @property fitness
 */
class EvaluatedIndividual(
    keys: DoubleArray,
    val fitness: Double
) : Individual(keys) {

    private constructor() : this(doubleArrayOf(), Double.MAX_VALUE)

    operator fun compareTo(other: EvaluatedIndividual): Int = this.fitness.compareTo(other.fitness)

    companion object {
        val DUMMY = EvaluatedIndividual()
    }
}




















