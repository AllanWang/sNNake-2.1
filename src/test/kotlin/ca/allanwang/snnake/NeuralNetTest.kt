package ca.allanwang.snnake

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by Allan Wang on 2017-05-16.
 */
class NeuralNetTest {

    @Test
    fun basicCreation() {
        val net = NeuralNet(1, 2, 1, 1.0, 2.0, 3.0, 4.0)
        val hidden = Matrix(1, 2, 1.0, 2.0)
        val output = Matrix(2, 1, 3.0, 4.0)
        assertEquals(hidden, net.hiddenWeightMatrix)
        assertEquals(output, net.outputWeightMatrix)
    }

    //Helper tests

    @Test
    fun random() {
        val rand = NeuralNet.randomValue()
        assertTrue(rand <= 1)
        assertTrue(rand >= -1)
    }
}