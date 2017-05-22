package ca.allanwang.snnake.randomness

import ca.allanwang.snnake.neuralnet.NNGenetics
import ca.allanwang.snnake.neuralnet.NeuralNet
import java.util.*

/**
 * Created by Allan Wang on 2017-05-21.
 *
 * The following class aims to optimize the NNGenetics implementation by checking the number of iterations needed to converge to a given result
 * as well as identifying the overlap rate between mutations
 */
fun main(args: Array<String>) {
    NNGeneticsOptimizer().start()
}


class NNGeneticsOptimizer(val size: Int = 200, val range: Int = 26, val iterations: Long = 100000000L) {
    val generationUpdate: (Int, List<Double>, Double) -> Unit = {
        generation, weights, fitness ->
        if (generation % 1000 == 0) {
            println("Generation $generation; fitness ${fitness * 100}%")
            if (size < 30) println(weights.map(this::normalize))
        }
    }
    val nng = NNGenetics("NNGO", NeuralNet(size, 1), iterations = 1, generationCallback = generationUpdate, writeToFile = false)

    val result = List(size, { rndnum })
    val rndnum: Int
        get() = normalize(rnd.nextDouble())

    fun normalize(d: Double): Int {
        var i = ((d * range) % range).toInt()
        if (i < 0) i += range
        return i
    }

    var counter = 0L

    fun start() {
        counter = 0L
        println("Data to match:\n$result")
        var data: List<Int> = emptyList()
        while (counter < iterations) {
            data = nng.net.getWeights().map(this::normalize)
            if (data == result) {
                println("Data matched after $counter attempts")
                println(data)
                return
            }
            nng.setFitnessOfCurrent(fitness(data))
            counter++
        }
        println("Data never matched after $iterations attempts")
        println("Closest weights: ${nng.bestData}")
        println(data)
    }

    fun fitness(list: List<Int>): Double {
        var delta = 0.0
        list.forEachIndexed { index, i -> delta += Math.abs(i - result[index]) }
        return ((range * size).toDouble() - delta) / (range * size).toDouble()
    }

    companion object {
        val rnd = Random()
    }
}