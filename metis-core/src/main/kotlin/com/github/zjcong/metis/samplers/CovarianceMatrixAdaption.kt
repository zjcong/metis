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

@file:Suppress("SpellCheckingInspection", "KDocUnresolvedReference")

package com.github.zjcong.metis.samplers

import com.github.zjcong.metis.*
import fr.inria.optimization.cmaes.CMAEvolutionStrategy

class CovarianceMatrixAdaption(
    dimensions: Int, seed: Int = 0, private val autoRestart: Boolean = false
) : Sampler(dimensions, seed) {

    private var cma: CMAEvolutionStrategy = CMAEvolutionStrategy()
    override val honorEntryPolicy: Boolean = false
    private val minimumPopulationSize: Int = 6

    override fun sample(population: Population): Population {
        if ((cma.stopConditions.number != 0)) {
            return if (autoRestart) {
                if (immigration == null) initializeWith(
                    Population(mutableListOf(population.bestIndividual())),
                    population.size
                )
                else initializeWith(Population(mutableListOf(immigration!!.last)), population.size)
            } else {
                population
            }
        }
        cma.updateDistribution(population.map { (it as EvaluatedIndividual).fitness }.toDoubleArray())
        return cma.samplePopulation().requireNoNulls().asPopulation()
    }

    private fun inriaCMAESInit(populationSize: Int) {
        cma = CMAEvolutionStrategy()
        cma.dimension = dimensions
        cma.setInitialX(0.50)
        cma.setInitialStandardDeviation(0.20)
        cma.parameters.populationSize = populationSize
        cma.options.stopTolFunHist = 1e-13
        cma.seed = seed.toLong()
        cma.options.verbosity = -2
    }

    override fun initialize(populationSize: Int): Population {
        inriaCMAESInit(populationSize.coerceAtLeast(minimumPopulationSize))
        cma.init()
        return cma.samplePopulation().requireNoNulls().asPopulation()
    }

    override fun initializeWith(population: Population, populationSize: Int): Population {
        inriaCMAESInit(populationSize.coerceAtLeast(minimumPopulationSize))
        cma.initialX = population[0].keys
        cma.init()
        return cma.samplePopulation().requireNoNulls().asPopulation()
    }
}