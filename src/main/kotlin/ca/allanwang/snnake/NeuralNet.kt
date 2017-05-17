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

class NeuralNet(inputSize: Int, hiddenSize: Int, outputSize: Int, var activation: Activation = Activation.SIGMOID) {

    val hiddenWeightMatrix = Matrix(inputSize, hiddenSize).forEach { _ -> randomValue() }
    val outputWeightMatrix = Matrix(hiddenSize, outputSize).forEach { _ -> randomValue() }
    val inputSize: Int
        get() = hiddenWeightMatrix.rows
    val hiddenSize: Int
        get() = hiddenWeightMatrix.cols
    val outputSize: Int
        get() = outputWeightMatrix.rows

    fun setWeights(vararg values: Double) {
        if (values.size != hiddenSize * (inputSize + outputSize))
            throw NeuralNetException(String.format("Neural Net values size mismatch; expecting %d doubles, but only found %d", hiddenSize * (inputSize + outputSize), values.size))
        val hiddenWeights = values.asList().subList(0, inputSize * hiddenSize).toDoubleArray()
        val outputWeights = values.asList().subList(inputSize * hiddenSize, values.size).toDoubleArray()
        hiddenWeightMatrix.set(Matrix(inputSize, hiddenSize, *hiddenWeights))
        outputWeightMatrix.set(Matrix(hiddenSize, outputSize, *outputWeights))
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
    fun forwardPropagate(input: Matrix): Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> {
        val hiddenActivity = input.clone() * hiddenWeightMatrix
        val hiddenActivation = hiddenActivity.clone().sigmoid()
        val outputActivity = hiddenActivation.clone() * outputWeightMatrix
        val outputActivation = outputActivity.clone().sigmoid()
        ((input * hiddenWeightMatrix).sigmoid() * outputWeightMatrix).sigmoid()
        return Pair(Pair(hiddenActivity, hiddenActivation), Pair(outputActivity, outputActivation))
    }

    fun costFunctionPrime(input: Matrix) {
        val resultData = forwardPropagate(input.clone())
    }

    override fun equals(other: Any?): Boolean = (other is NeuralNet && hiddenWeightMatrix == other.hiddenWeightMatrix && outputWeightMatrix == other.outputWeightMatrix)

    override fun hashCode(): Int = hiddenWeightMatrix.hashCode() + outputWeightMatrix.hashCode()

    override fun toString(): String = hiddenWeightMatrix.toString() + outputWeightMatrix.toString()

    companion object {
        /**
         * Returns random number between -1 & 1
         */
        fun randomValue() = Random().nextDouble() * 2 - 1

        fun sigmoid(value: Double): Double = 1.0 / (1.0 + Math.pow(Math.E, -value))

        fun sigmoidPrime(value: Double): Double = Math.pow(Math.E, -value) / Math.pow(1.0 + Math.pow(Math.E, -value), 2.0)
    }
}
}