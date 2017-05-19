package ca.allanwang.snnake

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 2017-05-16.
 */
class NeuralNetTest {

    lateinit var net: NeuralNet

    @Before
    fun init() {
        net = NeuralNet(2, 3, 1)
                .setWeights(1.0, 2.0, 3.0,
                        4.0, 5.0, 6.0,
                        -2.0,
                        3.0,
                        -4.0)
    }


    @Test
    fun forwardAndOutput() {
        val input = Matrix(3, 2,
                3, 5,
                5, 1,
                10, 2)
        assertEquals(net.forward(input).last().second, net.output(input), "Last forward activity should equal output")
    }

    @Test
    fun costFunction() {
        val trueResult = Matrix(1, 3, 5.0, 14.5, 22.5)
        val output = Matrix(2, 3, 1.0, 2.0, 3.0, 2.0, -3.0, 1.0)
        val result = Matrix(2, 3, 0.0, 0.0, 0.0, 5.0, 2.0, 7.0)
        assertEquals(trueResult, result.minus(output).forEach { value -> 0.5 * Math.pow(value, 2.0) }.sumRows())
    }

    @Test
    fun getSetFunction() {
        val data = net.getWeights()
        net.setWeights(data)
        assertTrue(data == net.getWeights(), "Get then set should yield the same data")
    }

}
