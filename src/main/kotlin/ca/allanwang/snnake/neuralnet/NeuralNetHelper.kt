package ca.allanwang.snnake.neuralnet

/**
 * Created by Allan Wang on 2017-05-18.
 *
 * Misc configurations for [NeuralNet]
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
    ONE {
        override fun random(): Double = rnd.nextDouble() * 2 - 1
    },
    /**
     * Returns random numbers with a mean of 0.0 and a stdev of 1.0; same as numpy.random.randn
     */
    GAUSSIAN {
        override fun random(): Double = rnd.nextGaussian()
    };

    abstract fun random(): Double

    companion object {
        val rnd = java.util.Random()
    }
}