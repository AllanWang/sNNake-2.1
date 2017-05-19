package ca.allanwang.snnake

/**
 * Created by Allan Wang on 2017-05-18.
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

//TODO
enum class Optimizer {
    BFGS;

    /**
     * [net]            NeuralNet containing weights and functions
     * [args]           Pair<input, output>
     * [maxIterations]  Maximum number of iterations to go through
     * [gtol]           Gradient norm max before termination
     * [eps]            Step size (default is sqrt of smallest recognizable step)
     */
    fun minimize(net: NeuralNet, args: Pair<Matrix, Matrix>?, maxIterations: Int = 200, gtol: Double = 1e-5, callback: ((List<Double>) -> Unit)?) {
        val weights = net.getWeights()
        val eps: Double = Math.sqrt(Math.ulp(1.0)) // step size
    }
}