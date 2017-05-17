package ca.allanwang.snnake

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by Allan Wang on 2017-05-16.
 *
 * Tests that either verify the NeuralNet creation or test methods that don't require the actual matrices
 */
class NeuralNetCoreTest {

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

    @Test
    fun sigmoidMax() {
        assertEquals("Sigmoid(max) == 1", 1.0, NeuralNet.sigmoid(Double.MAX_VALUE), 0.001)
    }

    @Test
    fun sigmoidMin() {
        assertEquals("Sigmoid(min) == 0", 0.0, NeuralNet.sigmoid(-Double.MAX_VALUE), 0.001)
    }

    @Test
    fun sigmoidZero() {
        assertEquals("Sigmoid(0) == 0.5", 0.5, NeuralNet.sigmoid(0.0), 0.001)
    }

    @Test
    fun sigmoidPrimeMax() {
        assertEquals("Sigmoid'(max) == 0", 0.0, NeuralNet.sigmoidPrime(Double.MAX_VALUE), 0.001)
    }

    //Exponents get too big to compute, but -99 serves its purpose
    @Test
    fun sigmoidPrimeMin() {
        assertEquals("Sigmoid'(min) == 0", 0.0, NeuralNet.sigmoidPrime(-99.0), 0.001)
    }

    @Test
    fun sigmoidPrimeZero() {
        assertEquals("Sigmoid'(0) == 0.5", 0.25, NeuralNet.sigmoidPrime(0.0), 0.001)
    }
}