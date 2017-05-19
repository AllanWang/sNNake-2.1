package ca.allanwang.snnake

import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 2017-05-17.
 */
class NumericalGradientCheck {

    val epsilon = 1e-4
    lateinit var net: NeuralNet
    lateinit var x: Matrix
    lateinit var y: Matrix

    @Before
    fun init() {
        x = Matrix(3, 2,
                3, 5,
                5, 1,
                10, 2)
        y = Matrix(3, 1, 75, 82, 93)
        x.normalize(Normalizer.COL_MAX)
        y / 100.0 // Max score is 100
        net = NeuralNet(2, 3, 1, activator = Activator.SIGMOID, random = Random.GAUSSIAN)
        net.setWeights(0.41711098, 1.20390897, 1.20588656, -0.7321096, -1.98205185, 0.76438889, -0.55641753, -1.76878453, 0.89061722)
    }

    @Test
    fun forward() {
        val x: Matrix = Matrix(3, 2, 0.3, 1.0, 0.5, 0.2, 1.0, 0.4)
        val yHat: Matrix = Matrix(3, 1, 0.54593348, 0.34168288, 0.34763383)
        val result = net.forward(x)
        assertTrue(yHat.equals(result.last().second, epsilon))
    }

    @Test
    fun numericalGradientCheck() {
        val numgrad = Matrix.toList(computeNumericalGradient())
        val grad = net.computeGradients(x, y)
        assertTrue(verifyEqual(grad, numgrad), "Numerical Gradient Check did not match for grad & numgrad")
    }

    fun verifyEqual(grad: List<Double>, numgrad: List<Double>): Boolean =
            if (grad.size != numgrad.size) false else (0..grad.size - 1).asSequence().all { i -> Math.abs(grad[i] - numgrad[i]) < 1e-8 }

    fun computeNumericalGradient(): Array<Matrix> {
        val weights = net.getWeights().toDoubleArray()
        val numgrad = Array<Matrix>(weights.size, { Matrix.EMPTY })
        val newWeights = weights.clone()
        for (i in 0..weights.size - 1) {
            newWeights[i] += epsilon
            net.setWeights(*newWeights)
            val loss2 = net.costFunction(x, y)
            newWeights[i] -= 2 * epsilon
            net.setWeights(*newWeights)
            val loss1 = net.costFunction(x, y)
            numgrad[i] = (loss2 - loss1) / (2 * epsilon)
            newWeights[i] += epsilon // reset
        }
        net.setWeights(*weights)
        return numgrad
    }
}