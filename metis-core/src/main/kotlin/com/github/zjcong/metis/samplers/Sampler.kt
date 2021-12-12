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

package com.github.zjcong.metis.samplers

import com.github.zjcong.metis.EntryPolicy
import com.github.zjcong.metis.EvaluatedIndividual
import com.github.zjcong.metis.Population
import java.io.Serializable


/**
 * TODO
 *
 * @property dimensions
 * @property seed
 */
abstract class Sampler(val dimensions: Int, val seed: Int) : Serializable {

    init {
        require(dimensions > 0) { "Dimensions must be greater than zero" }
    }

    abstract val honorEntryPolicy: Boolean

    protected var immigration: Immigration? = null

    abstract fun sample(population: Population): Population

    abstract fun initialize(populationSize: Int): Population

    abstract fun initializeWith(population: Population, populationSize: Int): Population


    /**
     * Iterate
     *
     * @param population
     * @param entryPolicy
     * @return
     */
    fun iterate(population: Population, entryPolicy: EntryPolicy): Population {
        if (honorEntryPolicy && immigration != null) entryPolicy(population, immigration!!)
        return sample(population)
    }

    /**
     * Immigrant arrival
     *
     * @param individual
     */
    open fun arrival(individual: EvaluatedIndividual) {
        if (immigration != null) {
            immigration!!.last = individual
            if (individual <= immigration!!.best) immigration!!.best = individual
        } else {
            immigration = Immigration(individual, individual)
        }

    }

    /**
     * Immigration
     *
     * @property best
     * @property last
     */
    data class Immigration(
        var best: EvaluatedIndividual,
        var last: EvaluatedIndividual,
    ) : Serializable
}