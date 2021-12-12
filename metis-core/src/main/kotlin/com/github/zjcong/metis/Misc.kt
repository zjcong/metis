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

import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Vector plus
 */
fun DoubleArray.vectorPlus(a: DoubleArray): DoubleArray {
    require(a.size == this.size) { "${this.size} != ${a.size}" }
    return DoubleArray(a.size) { this[it] + a[it] }
}

/**
 * Vector minus
 */
fun DoubleArray.vectorMinus(a: DoubleArray): DoubleArray {
    require(a.size == this.size) { "${this.size} != ${a.size}" }
    return DoubleArray(a.size) { this[it] - a[it] }
}

/**
 * Scalar multiply vector
 */
operator fun Double.times(a: DoubleArray): DoubleArray = DoubleArray(a.size) { a[it] * this }


/**
 * Maps a double value to a value of a given double range
 * @param range Range
 * @return value
 */
fun Double.valueIn(range: ClosedFloatingPointRange<Double>): Double {
    val r = range.endInclusive - range.start
    require(r.isFinite()) { "Infinite range" }
    require(!r.isNaN()) { "Invalid range" }
    return range.start + r * this
}


/**
 * Maps a double value to an element of a given list
 * @param list List
 * @return element
 */
fun <T> Double.elementIn(list: List<T>): T = list[this.valueIn(list.indices)]


/**
 * Maps a double value to a value in a integer range
 * @param range Integer range
 * @return Integer
 */
fun Double.valueIn(range: IntRange): Int =
    this.valueIn(range.first.toDouble().rangeTo(range.last.toDouble())).roundToInt()


/**
 * Maps a double value to a value of a Long range
 * @param range Range of long
 * @return value
 */
fun Double.valueIn(range: LongRange): Long =
    this.valueIn(range.first.toDouble().rangeTo(range.last.toDouble())).roundToLong()

