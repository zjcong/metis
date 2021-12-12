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

package com.github.zjcong.metis.samplers

import com.github.zjcong.metis.*
import com.github.zjcong.metis.times
import com.github.zjcong.metis.vectorMinus
import com.github.zjcong.metis.vectorPlus
import kotlin.random.Random


/**
 * Particle swamp optimization
 */
class ParticleSwampOptimization(
    dimensions: Int,
    private val w: Double = 0.792,
    private val c1: Double = 1.49,
    private val c2: Double = 1.49,
    seed: Int = 0
) : Sampler(dimensions, seed) {

    override val honorEntryPolicy: Boolean = true

    private val rng = Random(seed)
    private var pBest: Array<EvaluatedIndividual> = arrayOf()
    private var gBest: EvaluatedIndividual = EvaluatedIndividual.DUMMY
    private var velocities: Array<DoubleArray> = arrayOf()

    private val minimumPopulationSize = 5

    /**
     *
     */
    private fun updateVelocities(population: Population) {
        population.withIndex().forEach { i ->
            val pb = pBest[i.index].keys
            val v = velocities[i.index]
            val r1 = rng.nextDouble()
            val r2 = rng.nextDouble()
            val x = i.value.keys
            val vn = (w * v).vectorPlus(c1 * r1 * (pb.vectorMinus(x))).vectorPlus(c2 * r2 * (gBest.keys.vectorMinus(x)))
            velocities[i.index] = vn
        }
    }


    /**
     *
     */
    override fun sample(population: Population): Population {

        //update pBest and gBest
        population.withIndex().forEach { i ->
            if (i.value as EvaluatedIndividual <= pBest[i.index]) pBest[i.index] = i.value as EvaluatedIndividual
            if (i.value as EvaluatedIndividual <= gBest) gBest = i.value as EvaluatedIndividual
        }
        //update velocities
        updateVelocities(population)
        return population.withIndex().map {
            (it.value.keys.vectorPlus(velocities[it.index])).asIndividual()
        }.asPopulation()

    }

    /**
     * TODO
     *
     * @return
     */
    override fun initialize(populationSize: Int): Population {
        pBest = (0 until populationSize.coerceAtLeast(minimumPopulationSize)).map { EvaluatedIndividual.DUMMY }
            .toTypedArray()
        gBest = EvaluatedIndividual.DUMMY
        velocities = (0 until populationSize.coerceAtLeast(minimumPopulationSize)).map {
            DoubleArray(dimensions) { rng.nextDouble() }
        }.toTypedArray()
        return Population(populationSize.coerceAtLeast(minimumPopulationSize)) {
            Individual(dimensions) { rng.nextDouble(0.0, 1.0) }
        }
    }

    /**
     * TODO
     *
     * @param population
     * @param populationSize
     * @return
     */
    override fun initializeWith(population: Population, populationSize: Int): Population {
        val randomPopulation = initialize(populationSize)
        (0 until randomPopulation.size).forEach {
            if (it < population.size) randomPopulation[it] = population[it]
        }
        return randomPopulation
    }
}
