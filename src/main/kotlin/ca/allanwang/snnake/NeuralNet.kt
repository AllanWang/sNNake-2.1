package ca.allanwang.snnake

/**
 * Created by Allan Wang on 2017-05-15.
 */
class NeuralNetException(message: String) : RuntimeException(message)

enum class Activator {
    SIGMOID {
        override val activate: (value: Double) -> Double = { 1.0 / (1.0 + Math.exp(-it)) }
        override val activatePrime: (value: Double) -> Double = { Math.exp(-it) / Math.pow(1.0 + Math.exp(-it), 2.0) }
    };

    abstract val activate: (value: Double) -> Double
    abstract val activatePrime: (value: Double) -> Double
}

enum class Random {
    /**
     * Returns random number between -1 & 1
     */
    ABS_ONE {
        override fun random(): Double = java.util.Random().nextDouble() * 2 - 1
    },
    /**
     * Returns random numbers with a mean of 0.0 and a stdev of 1.0; same as numpy.random.randn
     */
    GAUSSIAN {
        override fun random(): Double = java.util.Random().nextGaussian()
    };

    abstract fun random(): Double
}

class NeuralNet(vararg layerSizes: Int, var activator: Activator = Activator.SIGMOID, var random: Random = Random.GAUSSIAN) {

    val matrices = Array<Matrix>(layerSizes.size - 1, { i -> Matrix(layerSizes[i], layerSizes[i + 1]).forEach { _ -> random.random() } })
    fun layerSize(i: Int) = matrices[i].rows
    val inputSize: Int
        get() = matrices.first().rows
    val outputSize: Int
        get() = matrices.last().rows
    val weightCount: Int
        get() = matrices.sumBy { matrix -> matrix.size }

    operator fun get(i: Int): Matrix = matrices[i]

    fun setWeights(vararg values: Double): NeuralNet {
        val iter = values.iterator()
        matrices.forEach {
            matrix ->
            matrix.forEach { _ -> if (iter.hasNext()) iter.nextDouble() else throw NeuralNetException("Could not set weights for all matrices; size mismatch") }
        }
        if (iter.hasNext()) throw NeuralNetException("Too many weights given in setWeights")
        return this
    }

    fun getWeights(): DoubleArray {
        var weights = doubleArrayOf()
        matrices.forEach {
            matrix ->
            weights += matrix.toArray()
        }
        return weights
    }

    fun setWeights(index: Int, vararg values: Double): NeuralNet {
        val iter = values.iterator()
        matrices[index].forEach { _ -> if (iter.hasNext()) iter.nextDouble() else throw NeuralNetException("Could not set weights for all matrices; size mismatch") }
        return this
    }

    /**
     * Propagates [input] data through the neural net and returns the outputs at each stage
     * [input] should be a matrix where each row contains values for each respective input node
     * in other words, its column size should equal the input layer size; the row size depends on the amount of data you wish to give
     *
     * Returned value contains a list of matrix pairs
     * The list starts with a pair for the first hidden layer and continues all the way until the output layer
     * Within those pairs, the first matrix is the activation matrix and the second matrix is the activity matrix
     * Activation   (z) = input * weight matrix
     * Activity     (a) = input * activator function
     *
     * Note that the very last activated matrix is our output/estimate
     */
    fun forward(input: Matrix): MutableList<Pair<Matrix, Matrix>> {
        val list = mutableListOf<Pair<Matrix, Matrix>>()
        val data = input.clone()
        matrices.forEach {
            matrix ->
            val activity = (data * matrix).clone() //ensure this activity no longer changes; data however will be affected cumulatively
            val activation = activity.clone().forEach(activator.activate)
            list.add(Pair(activity, activation))
            data.set(activation)
        }
        return list
    }

    /**
     * Passes input forward and computes cost difference
     * Sigma 0.5 * (y - yHat)^2
     */
    fun costFunction(input: Matrix, output: Matrix): Matrix {
        val result = forward(input).last().second
        return result.minus(output.clone()).forEach { value -> 0.5 * Math.pow(value, 2.0) }.sumRows()
    }

    /**
     * Computes the partial costs for each layer
     *
     * Let J by our cost, y be our actual output, and yHat be our calculated output
     * For each i in 0..max, where max = # of layers - 1, delta(J_i)/delta(W_i) = (a_i)^T * (-y - yHat) * f'(z_(i+1))
     *
     * Within our forward propagation list, we have activities (a) and activations (z) from 1..max + 1
     * We don't have a_0 in that list, but it is actually our output matrix. z_0 does not exist and is not used
     *
     * We see that -(y - yHat) is used within each iteration, so we may assign that to a matrix diff
     * We start with i in max downTo 0, so we may reverse the list from [forward] and also append our input as a_0
     * For the sake of pairing, z_0 will be a blank matrix of size 1
     *
     * We will iterate through each i to calculate the cost deltas, and add them to our list.
     * We will then reverse the list to retain input to output order
     */
    fun costFunctionPrime(input: Matrix, output: Matrix): MutableList<Matrix> {
        val costList = mutableListOf<Matrix>()
        val resultData = forward(input)
        resultData.reverse()
        resultData.add(Pair(Matrix(1, 1), input.clone()))
        val diff = -(output.clone() - resultData.first().second)  // -(y - yHat), where yHat = a_last
        resultData.forEach {
            pair ->
            costList.add(pair.second.transpose() * (diff.clone().forEach(activator.activatePrime)))
        }
        costList.reverse()
        return costList
    }

    override fun equals(other: Any?): Boolean = (other is NeuralNet &&
            inputSize == other.inputSize && outputSize == other.outputSize && matrices contentDeepEquals other.matrices)

    fun equals(other: Any?, maxDiff: Double): Boolean = (other is NeuralNet &&
            inputSize == other.inputSize && outputSize == other.outputSize && matrices.size != other.matrices.size &&
            (0..matrices.size - 1).all { matrices[it].equals(other.matrices[it], maxDiff) })

    override fun hashCode(): Int = matrices.contentDeepHashCode()

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("NN: ${matrices.size + 1} layers\n")
        builder.append("$inputSize input neurons; $outputSize output neurons\n")
        matrices.forEach { matrix -> builder.append(matrix.toString()) }
        return builder.toString()
    }

}
