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

    fun setWeights(values: List<Double>): NeuralNet {
        val iter = values.iterator()
        matrices.forEach { matrix -> matrix.forEach { _ -> if (iter.hasNext()) iter.next() else throw NeuralNetException("Could not set weights for all matrices; size mismatch") } }
        if (iter.hasNext()) throw NeuralNetException("Too many weights given in setWeights")
        return this
    }

    fun setWeights(vararg values: Double): NeuralNet = setWeights(values.toList())

    fun getWeights(): List<Double> {
        val weights = mutableListOf<Double>()
        matrices.forEach { matrix -> weights.addAll(matrix.toList()) }
        return weights
    }

    fun setWeights(index: Int, values: List<Double>): NeuralNet {
        val iter = values.iterator()
        matrices[index].forEach { _ -> if (iter.hasNext()) iter.next() else throw NeuralNetException("Could not set weights for all matrices; size mismatch") }
        return this
    }

    fun setWeights(index: Int, vararg values: Double): NeuralNet = setWeights(index, values.toList())

    /**
     * Propagates [input] data through the neural net and returns the outputs at each stage
     * [input] should be a matrix where each row contains values for each respective input node
     * in other words, its column size should equal the input layer size; the row size depends on the amount of data you wish to give
     *
     * Returned value contains a list of matrix pairs
     * The list starts with a pair for the first hidden layer and continues all the way until the output layer
     * Within those pairs, the first matrix is the activation matrix and the second matrix is the activity matrix
     * Activation   (z) = data * weight matrix
     * Activity     (a) = data * activator function
     * Data             = input at first, then activity
     *
     * Note that the very last activated matrix is our output/estimate
     */
    fun forward(input: Matrix): MutableList<Pair<Matrix, Matrix>> {
        val list = mutableListOf<Pair<Matrix, Matrix>>()
        val data = input.clone()
        matrices.forEach {
            matrix ->
            val activity = (data * matrix).clone()
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
     * Let J by our cost, x be our input, y be our actual output, and yHat be our calculated output
     * We may note from forward propagation that we have activations (z) & activities (a) from 2..max, where max = # of layers
     * Note that the a_max = our output yHat, and that a_1, which doesn't exist, is really our input x
     * For each i in 1..max, where max = # of layers - 1, delta(J)/delta(W_i) = -(y - yHat) * delta(yHat)/delta(W_i)
     * Note that delta(yHat)/delta(W_i) = delta(a_max)/delta(z_max) * delta(z_max)/delta(W_i)
     * The first fraction becomes f'(z_max)
     * The second fraction can be split using the chain rule into the following components:
     * delta(z_k)/delta(w_{k-1})    which equals    a_{k-1}         note that z_k = a_{k-1} * w_{k-1}
     * delta(z_k)/delta(a_{k-1})    which equals    w_{k-1}^T
     * delta(a_k)/delta(z_k)        which equals    f'(z_k), where f' is the activatePrime function
     *
     * We may observe the following pattern: delta(z_max)/delta(W_i) = W_{max-1}^T * f'(z_{max-1}) * ... * W_{i+1}^T * f'(z_{i+1}) * a_i
     * We may reuse the portion from max to i + 1 by saving it as a delta
     * Every subsequent step will be (delta scalarMultiply W_k) times z_k, then cost = (a_1^T times delta)
     *
     */
    fun costFunctionPrime(input: Matrix, output: Matrix): MutableList<Matrix> {
        val costList = mutableListOf<Matrix>()
        val resultData = forward(input)
        resultData.add(0, Pair(Matrix.EMPTY, input.clone()))
        var delta = Matrix.EMPTY
        for (i in resultData.size - 1 downTo 1) {
            val pair = resultData[i]
            if (i == resultData.size - 1) delta = (-(output.clone() - pair.second))   // -(y - yHat)
            else (delta * matrices[i].clone().transpose())                      // delta * W^T
            delta.scalarMultiply(pair.first.forEach(activator.activatePrime))   // delta x f'(z)
            costList.add(resultData[i - 1].second.transpose() * delta)                       // a^T * delta
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
