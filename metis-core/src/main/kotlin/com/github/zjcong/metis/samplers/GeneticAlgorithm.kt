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

@file:Suppress("unused")

package com.github.zjcong.metis.samplers

import com.github.zjcong.metis.*
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * TODO
 *
 * @property elites
 * @property mutants
 * @property bias
 * @constructor
 * TODO
 *
 * @param dimensions
 * @param seed
 */
class GeneticAlgorithm(
    dimensions: Int,
    private val elites: Double = 0.25,
    private val mutants: Double = 0.20,
    private val bias: Double = 0.7,
    seed: Int = 0,
) : Sampler(dimensions, seed) {

    override val honorEntryPolicy: Boolean = true
    private var populationSize: Int = 0
    private val rng = Random(seed)

    override fun sample(population: Population): Population {

        val nextGeneration: MutableList<Individual> = mutableListOf()

        val sortedPop = population.sortedBy { (it as EvaluatedIndividual).fitness }

        val nElites = (populationSize * elites).roundToInt()
        val nMutants = (populationSize * mutants).roundToInt()

        // Copy elites
        repeat(nElites) { nextGeneration.add(sortedPop[it]) }

        // Generate Mutants
        repeat(nMutants) { nextGeneration.add(Individual(dimensions) { rng.nextDouble() }) }

        // Crossover
        repeat(populationSize - nElites - nMutants) {
            val eliteParent = sortedPop[rng.nextInt(0, nElites)]
            val normalParent = sortedPop[rng.nextInt(nElites, this.populationSize)]
            val child = DoubleArray(dimensions) { i ->
                if (rng.nextDouble() < bias) eliteParent[i] else normalParent[i]
            }
            nextGeneration.add(child.asIndividual())
        }
        return nextGeneration.asPopulation()
    }


    override fun initialize(populationSize: Int): Population {
        this.populationSize = populationSize.coerceAtLeast(10)
        return Population(populationSize.coerceAtLeast(10)) {
            Individual(dimensions) { rng.nextDouble(0.0, 1.0) }
        }
    }


    override fun initializeWith(population: Population, populationSize: Int): Population {
        val randomPopulation = initialize(populationSize)
        (0 until randomPopulation.size).forEach {
            if (it < population.size) randomPopulation[it] = population[it]
        }
        return randomPopulation
    }
}