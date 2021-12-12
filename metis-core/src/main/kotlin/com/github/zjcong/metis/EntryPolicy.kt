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

import com.github.zjcong.metis.samplers.Sampler
import java.io.Serializable
import kotlin.random.Random


/**
 * Entry Policy
 *
 */
fun interface EntryPolicy : Serializable {
    operator fun invoke(evaluatedPopulation: Population, immigration: Sampler.Immigration)

    companion object {

        val CLOSED_BORDER = EntryPolicy { _, _ -> }

        val LAST_TO_RANDOM = EntryPolicy { evaluatedPopulation, immigration ->
            evaluatedPopulation[Random.nextInt(0, evaluatedPopulation.size)] = immigration.last
        }

        val BEST_TO_WORST = EntryPolicy { evaluatedPopulation, immigration ->
            val worstIndex =
                evaluatedPopulation.withIndex().maxByOrNull { (it.value as EvaluatedIndividual).fitness }!!.index
            evaluatedPopulation[worstIndex] = immigration.best
        }

        val LAST_TO_WORST = EntryPolicy { evaluatedPopulation, immigration ->
            val worstIndex =
                evaluatedPopulation.withIndex().maxByOrNull { (it.value as EvaluatedIndividual).fitness }!!.index
            evaluatedPopulation[worstIndex] = immigration.last
        }

        val BEST_TO_WORST_CONDITIONAL = EntryPolicy { evaluatedPopulation, immigration ->
            val worstIndex =
                evaluatedPopulation.withIndex().maxByOrNull { (it.value as EvaluatedIndividual).fitness }!!.index
            if (immigration.best <= evaluatedPopulation.worstIndividual())
                evaluatedPopulation[worstIndex] = immigration.best
        }
    }
}

