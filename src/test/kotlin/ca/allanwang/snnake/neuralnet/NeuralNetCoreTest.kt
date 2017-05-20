package ca.allanwang.snnake.neuralnet

import ca.allanwang.snnake.assertDoubleEquals
import ca.allanwang.snnake.assertDoubleNotEquals
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Created by Allan Wang on 2017-05-16.
 *
 * Tests that either verify the NeuralNet creation or test methods that don't require the actual matrices
 */
class NeuralNetCoreTest {

    @Test
    fun basicCreation() {
        val net = NeuralNet(1, 2, 1)
                .setWeights(1.0, 2.0, 3.0, 4.0)
        val hidden = Matrix(1, 2, 1.0, 2.0)
        val output = Matrix(2, 1, 3.0, 4.0)
        assertEquals(hidden, net[0])
        assertEquals(output, net[1])
    }

    @Test
    fun notEnoughWeights() {
        try {
            NeuralNet(1, 2, 1).setWeights(1.0, 2.0, 3.0)
            fail("Did not catch too few variables in setWeights")
        } catch (e: NeuralNetException) {
            assertEquals("Could not set weights for all matrices; size mismatch", e.message)
        }
    }

    @Test
    fun tooManyWeights() {
        try {
            NeuralNet(1, 2, 1).setWeights(1.0, 2.0, 3.0, 4.0, 5.0)
            fail("Did not catch too many variables in setWeights")
        } catch (e: NeuralNetException) {
            assertEquals("Too many weights given in setWeights", e.message)
        }
    }

    //Helper tests

    @Test
    fun sigmoidMax() {
        assertDoubleEquals(1.0, Activator.SIGMOID.activate(Double.MAX_VALUE), "Sigmoid(max) == 1")
    }

    @Test
    fun sigmoidMin() {
        assertDoubleEquals(0.0, Activator.SIGMOID.activate(-Double.MAX_VALUE), "Sigmoid(min) == 0")
    }

    @Test
    fun sigmoidZero() {
        assertDoubleEquals(0.5, Activator.SIGMOID.activate(0.0), "Sigmoid(0) == 0.5")
    }

    @Test
    fun sigmoidPrimeMax() {
        assertDoubleEquals(0.0, Activator.SIGMOID.activatePrime(Double.MAX_VALUE), "Sigmoid'(max) == 0")
    }

    //Exponents random too big to compute, but -99 serves its purpose
    @Test
    fun sigmoidPrimeMin() {
        assertDoubleEquals(0.0, Activator.SIGMOID.activatePrime(-99.0), "Sigmoid'(min) == 0")
    }

    @Test
    fun sigmoidPrimeZero() {
        assertDoubleEquals(0.25, Activator.SIGMOID.activatePrime(0.0), "Sigmoid'(0) == 0.5")
    }

    //Ensure that random() is a function that doesn't cache the variable
    @Test
    fun randomAbsOneTest() {
        val a = Random.ONE.random()
        val b = Random.ONE.random()
        assertDoubleNotEquals(a, b, "Random variables should be different")
        assertTrue(a <= 1)
        assertTrue(a >= -1)
    }
}