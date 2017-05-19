package ca.allanwang.snnake

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 2017-05-18.
 */
class NNGeneticsTest {

    lateinit var nng: NNGenetics
    lateinit var first: List<Double>
    lateinit var second: List<Double>

    @Before
    fun init() {
        nng = NNGenetics("NNGT", NeuralNet(1, 2, 2).setWeights(1, 2, 3, 4, 5, 6), 20, 0.2, 2, intArrayOf(2, 4))
        first = listOf(1.0, 3.0, 5.0, 7.0, 9.0, 11.0)
        second = listOf(2.0, 4.0, 6.0, 8.0, 10.0, 12.0)
    }

    @Test
    fun files() {
        assertTrue(nng.bestFile.exists(), "Best file should be created")
        assertTrue(nng.populationFile.exists(), "Population file should be created")
    }

    @Test
    fun rw() {
        nng.clearFile(nng.populationFile)
        assertEquals(0, nng.read(nng.populationFile).size, "population file should be empty")
        nng.write(nng.populationFile, first)
        val data = nng.read(nng.populationFile)
        assertEquals(1, data.size, "population file should only have one list")
        assertEquals(first, data.first().first, "population file should have list we just wrote")
        assertEquals(-1, data.first().second, "data should have generation -1")
    }

    @Test
    fun crossOver() {
        val result = nng.crossover(first, second)
        val error = "Crossover should alternate from first to second using crossPoints, from 0 to list.size"
        assertEquals(first.subList(nng.crossPoints[0], nng.crossPoints[1]), result.subList(nng.crossPoints[0], nng.crossPoints[1]), error)
        assertEquals(second.subList(nng.crossPoints[1], nng.crossPoints[2]), result.subList(nng.crossPoints[1], nng.crossPoints[2]), error)
        assertEquals(first.subList(nng.crossPoints[2], first.size), result.subList(nng.crossPoints[2], first.size), error)
    }

    @Test
    fun breed() {
        val children = nng.breed(first, second)
        val parentSum = List(first.size, { first[it] + second[it] })
        val childSum = List(children.first.size, { children.first[it] + children.second[it] })
        assertEquals(parentSum, childSum, "Child sum in breeding should equal parent sum")
    }

    @Test
    fun mutate() {
        val firstMutable = first.toMutableList()
        nng.mutate(firstMutable)
        assertNotEquals(firstMutable, first, "Mutate directly should modify the list")
        val diff = List(first.size, { first[it] - firstMutable[it] })
        assertEquals(nng.mutationsPerList, diff.sumBy { v -> if (v == 0.0) 0 else 1 }, "mutate should change exactly ${nng.mutationsPerList} items")
    }

    @Test
    fun setFitnessOfCurrent() {
        //TODO
    }

    fun fillPopulation() {
        nng.populationMap.apply {
            put(List<Double>(6, { 2.0 }), 2)
            put(second.toList(), 3)
            put(first.toList(), 3)
            put(first.toList(), 4)
            assertEquals(first.toList(), maxBy { it.value }!!.key, "MaxEntry should be list of 3s")
        }
    }

    @Test
    fun updateGeneration() {
        nng.clearFile(nng.bestFile)
        fillPopulation()
        nng.updateGeneration()
        val best = Pair(first.toList(), 1)
        assertEquals(best, nng.read(nng.bestFile).first())
        assertEquals(2, nng.generation, "Generation should be incremented")
        assertEquals(0, nng.populationMap.size, "Population map should be cleared after updateGeneration")
        assertTrue(nng.generationSize <= nng.read(nng.populationFile).size, "population file should now hold at least ${nng.generationSize} items")
    }
}