package ca.allanwang.snnake

import java.util.*

/**
 * Created by Allan Wang on 2017-05-15.
 */
class NeuralNetException(message: String) : RuntimeException(message)

enum class Activation {
    SIGMOID {
        override fun activate(value: Double): Double = 1.0 / (1.0 + Math.pow(Math.E, -value))
        override fun activatePrime(value: Double): Double = Math.pow(Math.E, -value) / Math.pow(1.0 + Math.pow(Math.E, -value), 2.0)
    };

    abstract fun activate(value: Double): Double
    abstract fun activatePrime(value: Double): Double
}

class NeuralNet(vararg layerSizes: Int, var activation: Activation = Activation.SIGMOID) {

    val matrices = Array<Matrix>(layerSizes.size - 1, { i -> Matrix(layerSizes[i], layerSizes[i + 1]).forEach { _ -> randomValue() } })
    fun layerSize(i: Int) = matrices[i].rows

    fun setWeights(vararg values: Double) {
        val iter = values.iterator()
        matrices.forEach {
            matrix ->
            matrix.forEach { _ -> if (iter.hasNext()) iter.nextDouble() else throw NeuralNetException("Could not set weights for all matrices; size mismatch") }
        }
        if (iter.hasNext()) throw NeuralNetException("Too many weights given in setWeights")
    }

    fun setWeights(index: Int, vararg values: Double) {
        val iter = values.iterator()
        matrices[index].forEach { _ -> if (iter.hasNext()) iter.nextDouble() else throw NeuralNetException("Could not set weights for all matrices; size mismatch") }
    }

    /**
     * Propagates [input] data through the neural net and returns the outputs at each stage
     * [input] should be a matrix where each row contains values for each respective input node
     * in other words, its column size should equal the input layer size; the row size depends on the amount of data you wish to give
     *
     * Returned value contains a pair of matrix pairs
     * The first pair is for the hidden layer and the second pair is for the output layer
     * Within those pairs, the first matrix is the activity and the second matrix is the activation
     * Activity = input * weight matrix
     * Activation = input * sigmoid
     */
    fun forwardPropagate(input: Matrix): MutableList<Pair<Matrix, Matrix>> {
        val list = mutableListOf<Pair<Matrix, Matrix>>()
        val data = input.clone()
        matrices.forEach {
            matrix ->
            val activity = (data * matrix).clone()
            val activation = activity.clone().sigmoid()
            list.add(Pair(activity, activation))
        }
        return list
    }

    fun costFunctionPrime(input: Matrix) {
        val resultData = forwardPropagate(input.clone())
    }

    override fun equals(other: Any?): Boolean = (other is NeuralNet && matrices contentDeepEquals other.matrices)

    override fun hashCode(): Int = matrices.contentDeepHashCode()

    override fun toString(): String = matrices.contentToString()

    companion object {
        /**
         * Returns random number between -1 & 1
         */
        fun randomValue() = Random().nextDouble() * 2 - 1

        fun sigmoid(value: Double): Double = 1.0 / (1.0 + Math.pow(Math.E, -value))

        fun sigmoidPrime(value: Double): Double = Math.pow(Math.E, -value) / Math.pow(1.0 + Math.pow(Math.E, -value), 2.0)
    }
}
