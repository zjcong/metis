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

package com.github.zjcong.metis

import java.io.Serializable

/**
 * TODO
 *
 * @constructor
 * TODO
 *
 * @param individuals
 */
class Population(
    individuals: MutableList<Individual>
) : MutableList<Individual> by individuals, Serializable {

    val dimensions: Int
        get() {
            return this[0].dimensions
        }

    constructor(size: Int, init: (Int) -> Individual) : this((0 until size).map(init).toMutableList())
    constructor() : this(mutableListOf())

    fun isEvaluated(): Boolean {
        return all { it is EvaluatedIndividual }
    }

    fun associateWithFitness(fitness: DoubleArray): Population {
        require(isNotEmpty()) { "This population is empty" }
        require(fitness.isNotEmpty()) { "Fitness values are empty" }
        require(fitness.size == size) { "Inconsistent fitness values" }
        return this.withIndex().map { it.value.asEvaluated(fitness[it.index]) }.asPopulation()
    }
}

/**
 * TODO
 *
 * @return
 */
fun Population.bestIndividual(): EvaluatedIndividual {
    require(isNotEmpty()) { "Population is empty" }
    require(isEvaluated()) { "Some individuals have not been evaluated" }
    return minByOrNull { (it as EvaluatedIndividual).fitness } as EvaluatedIndividual
}

/**
 * TODO
 *
 * @return
 */
fun Population.worstIndividual(): EvaluatedIndividual {
    require(isNotEmpty()) { "Population is empty" }
    require(isEvaluated()) { "Some individuals have not been evaluated" }
    return minByOrNull { (it as EvaluatedIndividual).fitness } as EvaluatedIndividual
}

/**
 * TODO
 *
 * @return
 */
fun Collection<Individual>.asPopulation(): Population = Population(this.toMutableList())

/**
 * TODO
 *
 * @return
 */
fun Array<DoubleArray>.asPopulation(): Population = this.map { it.asIndividual() }.asPopulation()

