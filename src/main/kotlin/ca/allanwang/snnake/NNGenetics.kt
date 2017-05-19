package ca.allanwang.snnake

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.Random

/**
 * Created by Allan Wang on 2017-05-18.
 */
class NNGeneticsException(message: String) : RuntimeException(message)

class NNGenetics(key: String, val net: NeuralNet, val generationSize: Int = 20, val mutationRate: Double = 0.2, val mutationsPerList: Int = 2, crossPoints: IntArray = intArrayOf(2)) {
    var generation = 0
        private set
    private val resourceBase: String = javaClass.classLoader?.getResource("/")?.file ?: javaClass.getResource("/")!!.file
    val bestFile = file(resourceBase, "$key/$key.best.txt")
    val populationFile = file(resourceBase, "$key/$key.population.txt")
    val populationMap = hashMapOf<List<Double>, Int>()
    private val rnd = Random()
    lateinit var dataIter: Iterator<List<Double>> //TODO
    val crossPoints: IntArray

    init {
        val weightCount = net.weightCount
        if (mutationsPerList > weightCount) throw NNGeneticsException("Mutations per list ($mutationsPerList) exceeds weight count ($weightCount)")
        val crossPointList = crossPoints.sortedArray().toMutableList()
        if (crossPointList.first() < 0) throw NNGeneticsException("Crosspoints are indices and cannot be less than 0; cross ${crossPointList.first()} found")
        if (crossPointList.first() != 0) crossPointList.add(0, 0)
        this.crossPoints = crossPointList.toIntArray()
        if (generationSize < 0) throw NNGeneticsException("generationSize should be greater than 0; currently $generationSize")
    }

    fun setWeights(values: List<Double>): NNGenetics {
        net.setWeights(values)
        return this
    }

    /**
     * Given parents [first] & [second] of the same length
     * As well as [crossPoints] ranging from 0 to first.size inclusive,
     * Will generate a list of equal size by combining sublists of both parents based on the crossPoints, starting with the first parent
     */
    fun crossover(first: List<Double>, second: List<Double>, verify: Boolean = false): MutableList<Double> {
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
    fun breed(first: List<Double>, second: List<Double>): Pair<MutableList<Double>, MutableList<Double>> = Pair(crossover(first, second, true), crossover(second, first))

    /**
     * Given [list], will swap [mutationsPerList] number of items with numbers generated from [rand]
     */
    fun mutate(list: MutableList<Double>) {
        val set = hashSetOf<Int>()
        while (set.size < mutationsPerList)
            set.add(rand(list))
        set.forEach { i -> list[i] = rand() }
    }

    /**
     * Adds entry in [populationMap] with the current weights as the key and [fitness] as the value
     * Either updates the generation, sets new weights, or exits depending on count
     */
    fun setFitnessOfCurrent(fitness: Int) {
        populationMap.put(net.getWeights(), fitness)
        if (populationMap.size >= generationSize) updateGeneration()
        else if (dataIter.hasNext()) net.setWeights(dataIter.next())
        else {
            //TODO
        }
    }

    fun updateGeneration() {
        val maxEntry = populationMap.maxBy { it.value }
        var best: MutableList<List<Double>> = mutableListOf(*populationMap.keys.toTypedArray())
        best.sortBy { list -> populationMap[list] }     // sort by fitness
        best = best.subList(best.size / 2, best.size)   // get the better half
        populationMap.clear()
        clearFile(populationFile)
        //generate new population data
        writer(populationFile).use {
            w ->
            var count = 0
            while (count < generationSize) {
                val first = best[rand(best)]
                var second: List<Double>
                do {
                    second = best[rand(best)]
                } while (first == second)
                val children = breed(first, second)
                if (rnd.nextDouble() < mutationRate) mutate(children.first)
                w.println(listToString(children.first))
                if (rnd.nextDouble() < mutationRate) mutate(children.second)
                w.println(listToString(children.second))
                count += 2
            }
        }
        writer(bestFile).use { w -> w.println("$generation: ${listToString(maxEntry!!.key)}") }
    }

    fun runGeneration() {
        val bestData = read(bestFile)
        if (bestData.last().second != -1) generation = bestData.last().second
        generation++
        val populationData = read(populationFile)
        dataIter = read(populationFile).map { pair -> pair.first }.iterator()
    }

    internal fun listToString(list: List<Double>): String {
        val builder = StringBuilder()
        list.forEach { d -> builder.append(d).append(',') }
        return builder.trim().trimEnd(',').toString()
    }

    internal fun stringToList(s: String): List<Double> {
        val list = mutableListOf<Double>()
        s.trim().split(',').forEach { ss -> list.add(ss.toDouble()) }
        return list
    }

    internal fun file(vararg dir: String): File {
        val file = File(dir.joinToString(separator = "/", transform = { s -> s.trim('/') }))
        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        if (!file.exists())
            file.createNewFile()
        return file
    }

    internal fun write(file: File, vararg lists: List<Double>) = writer(file).use { w -> lists.forEach { list -> w.println(listToString(list)) } }
    internal fun writer(file: File, append: Boolean = true) = PrintWriter(BufferedWriter(FileWriter(file, append)))
    internal fun read(file: File): List<Pair<List<Double>, Int>> {
        val list = mutableListOf<Pair<List<Double>, Int>>()
        file.readLines().forEach {
            s ->
            if (!s.isBlank()) {
                var generation = -1
                var line = s
                if (s.contains(":")) {
                    val split = s.split(":")
                    generation = split[0].trim().toInt()
                    line = split[1]
                }
                list.add(Pair(stringToList(line), generation))
            }
        }
        return list
    }

    internal fun clearFile(file: File) = writer(file, false).use { w -> w.print("") }

    internal fun rand(): Double = net.randomWeight()
    internal fun rand(max: Int): Int = rnd.nextInt(max)
    internal fun rand(list: List<Any>): Int = rnd.nextInt(list.size)

}