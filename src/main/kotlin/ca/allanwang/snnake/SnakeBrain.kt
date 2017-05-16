package ca.allanwang.snnake

import java.util.*

/**
 * Created by Allan Wang on 2017-05-15.
 */
class NeuralNetException(message: String) : RuntimeException(message)

class NeuralNet(private val inputSize: Int, private val hiddenSize: Int, private val outputSize: Int, vararg values: Double) {

    val hiddenWeightMatrix: Matrix
    val outputWeightMatrix: Matrix

    init {
        if (!values.isEmpty() && values.size != hiddenSize * (inputSize + outputSize))
            throw NeuralNetException(String.format("Neural Net values size mismatch; expecting %d doubles, but only found %d", hiddenSize * (inputSize + outputSize), values.size))
        val hiddenWeights: DoubleArray
        val outputWeights: DoubleArray
        if (values.isEmpty()) {
            hiddenWeights = DoubleArray(inputSize * hiddenSize, { _ -> randomValue() })
            outputWeights = DoubleArray(hiddenSize * outputSize, { _ -> randomValue() })
        } else {
            hiddenWeights = values.asList().subList(0, inputSize * hiddenSize).toDoubleArray()
            outputWeights = values.asList().subList(inputSize * hiddenSize, values.size).toDoubleArray()
        }
        hiddenWeightMatrix = Matrix(inputSize, hiddenSize, *hiddenWeights)
        outputWeightMatrix = Matrix(hiddenSize, outputSize, *outputWeights)
    }

    fun propagate(m: Matrix): Matrix = ((m * hiddenWeightMatrix).sigmoid() * outputWeightMatrix).sigmoid()

    override fun equals(other: Any?): Boolean = (other is NeuralNet && hiddenWeightMatrix == other.hiddenWeightMatrix && outputWeightMatrix == other.outputWeightMatrix)

    override fun hashCode(): Int = hiddenWeightMatrix.hashCode() + outputWeightMatrix.hashCode()

    override fun toString(): String = hiddenWeightMatrix.toString() + outputWeightMatrix.toString()

    companion object {
        /**
         * Returns random number between -1 & 1
         */
        fun randomValue() = Random().nextDouble() * 2 - 1

        fun sigmoid(value: Double): Double = 1.0 / (1.0 + Math.pow(Math.E, -value))
    }
}