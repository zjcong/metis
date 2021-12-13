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

package com.github.zjcong.metis.problem

import com.github.zjcong.metis.Population

@Suppress("MemberVisibilityCanBePrivate")
abstract class MultiObjectiveProblem<T> : Problem<T> {

    abstract val objectives: Set<SingleObjectiveProblem<T>>


    /**
     * Evaluate a population
     *
     * @param population
     * @return
     */
    override operator fun invoke(population: Population): DoubleArray {
        objectives.map {  }
        TODO()
    }
}