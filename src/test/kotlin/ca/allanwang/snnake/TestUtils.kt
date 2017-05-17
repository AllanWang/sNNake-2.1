package ca.allanwang.snnake

import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 2017-05-16.
 */
fun doubleEquals(a: Double, b: Double) = Math.abs(a - b) < 0.0001

fun assertDoubleEquals(a: Double, b: Double, s: String?) {
    assertTrue(doubleEquals(a, b), s)
}