package ca.allanwang.snnake.snake

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 2017-05-21.
 */
class CTest{

    lateinit var head:C
    lateinit var ref:C

    @Before
    fun init() {
        head = C(2,2)
        ref = C(0,0)
    }

    @Test
    fun shift() {
        assertEquals(head.shift(1, 0, Directions.UP), head.shift(0, 1, Directions.RIGHT))
    }

    @Test
    fun delta() {
        assertEquals(head.delta(ref, 1, 0, Directions.UP), head.delta(ref, 0, 1, Directions.RIGHT))
    }
}