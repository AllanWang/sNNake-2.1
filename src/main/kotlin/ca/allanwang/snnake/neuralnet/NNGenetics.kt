package ca.allanwang.snnake.neuralnet

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

/**
 * Created by Allan Wang on 2017-05-18.
 */
class NNGeneticsException(message: String) : RuntimeException(message)

val resourceBase = File(System.getenv("AppData"), ".sNNake_2.1")

class NNGenetics(key: String,
                 val net: NeuralNet, // The Neural Net
                 val populationSize: Int = 100, // Max population size before a new generation
                 val populationRetention: Double = 0.3, // Percent of population to keep for next generation
                 val mutationRate: Double = 0.2, // Percent of population to mutate
                 val mutationsPerList: Int = 2, // Number of items to mutate per given weight set
                 val mutationIncrement: Double = 1e-3, // For increment mutations, max value to increment/decrement
                 val iterations: Int = 3, // Number of times to test a given weight before moving onto the next one
                 crossPoints: IntArray = intArrayOf(2), // Joints to cross two parents when creating their children
                 generationCallback: ((Int, List<Double>, Double) -> Unit)? = null, // Callback function to receive generation update reports
                 val writeToFile: Boolean = true // Write population and best data to files
) {

    var generationCallback: ((Int, List<Double>, Double) -> Unit)? = generationCallback
        set(value) {
            field = value
            value?.invoke(bestData.second, bestData.first, bestData.third)
        }
    var generation = -1
        private set
    private val iterationList = mutableListOf<Double>()
    internal val bestFile = file(resourceBase.absolutePath, "$key/$key.best.txt")
    internal val populationFile = file(resourceBase.absolutePath, "$key/$key.population.txt")
    internal val populationMap = hashMapOf<List<Double>, Double>()
    private val rnd = java.util.Random()
    var dataIter: Iterator<List<Double>> = emptyList<List<Double>>().iterator()
    lateinit var bestData: Triple<List<Double>, Int, Double>
    val crossPoints: IntArray

    init {
        val weightCount = net.weightCount
        if (mutationsPerList > weightCount) throw NNGeneticsException("Mutations per list ($mutationsPerList) exceeds weight count ($weightCount)")
        val crossPointList = crossPoints.sortedArray().toMutableList()
        if (crossPointList.first() < 0) throw NNGeneticsException("Crosspoints are indices and cannot be less than 0; cross ${crossPointList.first()} found")
        if (crossPointList.first() != 0) crossPointList.add(0, 0)
        this.crossPoints = crossPointList.toIntArray()
        if (iterations < 1) throw NNGeneticsException("iteration count should be at least 1; currently $iterations")
        if (populationSize < 2) throw NNGeneticsException("populationSize should be greater than 2; currently $populationSize")
        if (populationRetention < 0) throw NNGeneticsException("populationRetention should be greater than 0; currently $populationRetention")
        prepareGeneration()
    }

    fun getOutput(input: Matrix) = net.output(input)
    internal fun setWeights() = if (dataIter.hasNext()) net.setWeights(dataIter.next()) else net.setRandomWeights()

    /**
     * Given parents [first] & [second] of the same length
     * As well as [crossPoints] ranging from 0 to first.size inclusive,
     * Will generate a list of equal size by combining sublists of both parents based on the crossPoints, starting with the first parent
     */
    internal fun crossover(first: List<Double>, second: List<Double>, verify: Boolean = false): MutableList<Double> {
        if (verify) {
            if (first.size != second.size) throw NNGeneticsException("Crossover parents do not have the same size: ${first.size}, ${second.size}")
            if (first.size != net.weightCount) throw NNGeneticsException("Crossover parent size does not match weight count: ${first.size}, ${net.weightCount}")
            if (crossPoints.last() > first.size) throw NNGeneticsException("Crosspoints are indices and cannot exceed the max index of the parents; cross ${crossPoints.last()} found when parent size is ${first.size}")
        }
        val result = mutableListOf<Double>()
        var pickFirst = true
        for (i in 0..crossPoints.size - 2) {
            val sub = (if (pickFirst) first else second).subList(crossPoints[i], crossPoints[i + 1])
            result.addAll(sub)
            pickFirst = !pickFirst
        }
        if (crossPoints.last() < first.size) {
            val sub = (if (pickFirst) first else second).subList(crossPoints.last(), first.size)
            result.addAll(sub)
        }
        return result
    }

    /**
     * Returns a pair of children from two parents, using the given [crossPoints]
     * The first crossover starts with the first parent
     * The second crossover starts with the second parent
     */
    internal fun breed(first: List<Double>, second: List<Double>): Pair<MutableList<Double>, MutableList<Double>> = Pair(crossover(first, second, true), crossover(second, first))

    /**
     * Given [list], will swap [mutationsPerList] number of items with numbers generated from [rand]
     */
    internal fun mutate(list: MutableList<Double>) {
        val set = hashSetOf<Int>()
        while (set.size < mutationsPerList)
            set.add(rand(list))
        set.forEach { i -> list[i] = if (Random.ONE.random() < 0.0) rand() else list[i] + Random.ONE.random() * mutationIncrement }
    }

    /**
     * Adds entry in [populationMap] with the current weights as the key and [fitness] as the value
     * Either updates the generation, sets new weights, or exits depending on count
     */
    fun setFitnessOfCurrent(fitness: Double) {
        iterationList.add(fitness)
        if (iterationList.size >= iterations) {
            val aveFitness = iterationList.sum() / iterationList.size
            iterationList.clear()
            populationMap.put(net.getWeights(), aveFitness)
            if (populationMap.size >= populationSize) updateGeneration()
            else setWeights()
        }
    }

    internal fun updateGeneration() {
        val maxEntry = populationMap.maxBy { it.value }
        var best: MutableList<List<Double>> = mutableListOf(*populationMap.keys.toTypedArray())
        best.sortBy { list -> populationMap[list] }     // sort by fitness
        best = best.subList(Math.min((best.size.toDouble() * (1.0 - populationRetention)).toInt(), best.size - 2), best.size)   // get the better sublist
        populationMap.clear()
        if (writeToFile) clearFile(populationFile)
        //generate new population data
        val newPopulation = mutableListOf<List<Double>>()
        var count = 0
        while (count < populationSize) {
            val first = best[rand(best)]
            var second: List<Double>
            do {
                second = best[rand(best)]
            } while (first == second)
            val children = breed(first, second)
            if (rnd.nextDouble() < mutationRate) mutate(children.first)
            newPopulation.add(children.first)
            if (rnd.nextDouble() < mutationRate) mutate(children.second)
            newPopulation.add(children.second)
            count += 2
        }

        if (writeToFile) {
            writer(populationFile).use { w -> newPopulation.forEach { w.println(listToString(it)) } }
            writer(bestFile).use { w -> w.println("$generation: ${listToString(maxEntry!!.key)} # ${maxEntry.value}") }
        }
        dataIter = newPopulation.iterator()
        bestData = Triple(maxEntry!!.key, generation, maxEntry.value)
        generationCallback?.invoke(bestData.second, bestData.first, bestData.third)
        prepareGeneration()
    }

    internal fun prepareGeneration() {
        if (generation == -1) {
            bestData = getLastBest()
            generation = bestData.second
            generationCallback?.invoke(bestData.second, bestData.first, bestData.third)
        }
        generation++
        if (writeToFile && !dataIter.hasNext()) dataIter = readPopulation().iterator()
        setWeights()
    }

    internal fun listToString(list: List<Double>): String {
        val builder = StringBuilder()
        list.forEach { d -> builder.append(d).append(',') }
        return builder.trim().trimEnd(',').toString()
    }

    internal fun stringToList(s: String): List<Double> = s.trim().split(',').map(String::toDouble)

    internal fun file(vararg dir: String): File {
        val file = File(dir.joinToString(separator = "/", transform = { s -> s.trim('/') }))
        if (!writeToFile) return file
        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        if (!file.exists())
            file.createNewFile()
        return file
    }

    internal fun write(file: File, vararg lists: List<Double>) = writer(file).use { w -> lists.forEach { list -> w.println(listToString(list)) } }
    internal fun writer(file: File, append: Boolean = true) = PrintWriter(BufferedWriter(FileWriter(file, append)))
    /**
     * Reader helper, for best.txt
     * Extracts last line into a Triple containing the weights, the generation, and the fitness value
     */
    internal fun getLastBest(): Triple<List<Double>, Int, Double> {
        if (!writeToFile) return Triple(emptyList(), -1, 0.0)
        val lines = bestFile.readLines()
        if (lines.isEmpty() || lines.last().isBlank()) return Triple(emptyList(), -1, 0.0)
        val s = lines.last()
        var generation = -1
        var line = s
        if (s.contains(":")) {
            val split = s.split(":")
            generation = split[0].trim().toInt()
            line = split[1]
        }
        var fitness = 0.0
        if (line.contains('#')) {
            val split = line.split('#')
            line = split[0]
            fitness = split[1].trim().toDouble()
        }
        return Triple(stringToList(line), generation, fitness)
    }

    /**
     * Reader helper for population.txt
     * Extract each line and maps it into its weighting
     */
    internal fun readPopulation(): List<List<Double>> = populationFile.readLines().filter { s -> s.isNotBlank() }.map { s -> stringToList(s) }

    internal fun clearFile(file: File) {
        writer(file, false).use { w -> w.print("") }
        if (file == bestFile) generation = 0
    }

    internal fun rand(): Double = net.randomWeight()
    internal fun rand(max: Int): Int = rnd.nextInt(max)
    internal fun rand(list: List<Any>): Int = rnd.nextInt(list.size)

}