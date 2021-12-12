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

@file:Suppress("unused", "FunctionName")

package com.github.zjcong.metis.samplers

import com.github.zjcong.metis.*
import com.github.zjcong.metis.times
import com.github.zjcong.metis.vectorMinus
import com.github.zjcong.metis.vectorPlus
import kotlin.random.Random

/**
 *
 */
class DifferentialEvolution constructor(
    dimensions: Int,
    private val cr: Double = 0.8,
    private val mutation: (Array<DoubleArray>, DoubleArray, Random) -> Array<DoubleArray> = BEST_1(0.7),
    seed: Int = 0
) : Sampler(dimensions, seed) {


    private val rng = Random(seed)

    private var pSize = 0
    override val honorEntryPolicy: Boolean = true
    private val minimalPopulation: Int = 8

    /**
     *
     */
    override fun sample(population: Population): Population {

        val chunked = population.chunked(pSize)
        val selected = chunked[0].indices.map {
            if (chunked[0][it] as EvaluatedIndividual <= chunked[1][it] as EvaluatedIndividual) chunked[0][it]
            else chunked[1][it]
        }
        val n = selected.map { it.keys }.toTypedArray()
        val fn = selected.map { (it as EvaluatedIndividual).fitness }.toDoubleArray()
        // Mutation and crossover
        val tn = mutation(n, fn, rng).withIndex().map { m ->
            check(m.value.size == dimensions)
            val i = m.index
            val mutation = m.value
            val jRand = rng.nextInt(0, dimensions)
            DoubleArray(dimensions) { j ->
                if (rng.nextDouble() < cr || j == jRand) mutation[j]
                else n[i][j]
            }
        }.toTypedArray()

        return n.plus(tn).asPopulation()
    }


    override fun initialize(populationSize: Int): Population {
        pSize = populationSize.coerceAtLeast(minimalPopulation) / 2
        return Population(populationSize.coerceAtLeast(minimalPopulation)) {
            Individual(dimensions) { rng.nextDouble() }
        }
    }

    override fun initializeWith(population: Population, populationSize: Int): Population {
        val randomPopulation = initialize(populationSize)
        (0 until randomPopulation.size).forEach {
            if (it < population.size) randomPopulation[it] = population[it]
        }
        return randomPopulation
    }


    /**
     * Mutation Strategies
     */
    companion object {
        /**
         * DE/rand/1
         */
        fun RAND_1(f: Double): (Array<DoubleArray>, DoubleArray, Random) -> Array<DoubleArray> =
            fun(g: Array<DoubleArray>, _: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 3)
                return Array(g.size) {
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 3) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    parents[0].vectorPlus(f * (parents[1].vectorMinus(parents[2])))
                }
            }


        /**
         * DE/best/1
         */
        fun BEST_1(f: Double): (Array<DoubleArray>, DoubleArray, Random) -> Array<DoubleArray> =
            fun(g: Array<DoubleArray>, fit: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 3)
                val best = g[fit.withIndex().minByOrNull { it.value }!!.index]
                return Array(g.size) {
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 2) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    best.vectorPlus(f * (parents[0].vectorMinus(parents[1])))
                }
            }


        /**
         * DE/best/2
         */
        fun BEST_2(f1: Double, f2: Double): (Array<DoubleArray>, DoubleArray, Random) -> Array<DoubleArray> =
            fun(g: Array<DoubleArray>, fit: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 5)
                val best = g[fit.withIndex().minByOrNull { it.value }!!.index]
                return Array(g.size) {
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 4) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    best.vectorPlus(f1 * (parents[0].vectorMinus(parents[1])))
                        .vectorPlus(f2 * (parents[2].vectorMinus(parents[3])))
                }
            }


        /**
         * DE/current-to-rand/1
         */
        fun CURRENT_TO_RAND_1(f1: Double, f2: Double): (Array<DoubleArray>, DoubleArray, Random) -> Array<DoubleArray> =
            fun(g: Array<DoubleArray>, _: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 5)
                return Array(g.size) { i ->
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 4) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    g[i].vectorPlus(f1 * (parents[0].vectorMinus(parents[1])))
                        .vectorPlus(f2 * (parents[2].vectorMinus(parents[3])))
                }
            }

        /**
         * DE/current-to-best/1
         */
        fun CURRENT_TO_BEST_1(f1: Double, f2: Double): (Array<DoubleArray>, DoubleArray, Random) -> Array<DoubleArray> =
            fun(g: Array<DoubleArray>, fit: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 5)
                val best = g[fit.withIndex().minByOrNull { it.value }!!.index]
                return Array(g.size) { i ->
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 4) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    g[i].vectorPlus(f1 * (best.vectorMinus(parents[1])))
                        .vectorPlus(f2 * (parents[2].vectorMinus(parents[3])))
                }
            }
    }
}