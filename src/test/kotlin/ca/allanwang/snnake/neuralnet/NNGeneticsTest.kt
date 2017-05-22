package ca.allanwang.snnake.neuralnet

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
        nng = NNGenetics("NNGT", NeuralNet(1, 2, 2).setWeights(1, 2, 3, 4, 5, 6))
        first = listOf(1.0, 3.0, 5.0, 7.0, 9.0, 11.0)
        second = listOf(2.0, 4.0, 6.0, 8.0, 10.0, 12.0)
    }

    @Test
    fun files() {
        assertTrue(nng.bestFile.exists(), "Best file should be created")
        assertTrue(nng.populationFile.exists(), "Population file should be created")
    }

//    @Test
//    fun resourceFiles() {
//        println(nng.bestFile.absolutePath)
//    }

    @Test
    fun rw() {
        nng.clearFile(nng.populationFile)
        assertEquals(0, nng.readPopulation().size, "population file should be empty")
        nng.write(nng.populationFile, first)
        val data = nng.readPopulation()
        assertEquals(1, data.size, "population file should only have one list")
        assertEquals(first, data.first(), "population file should have list we just wrote")
    }

    @Test
    fun mutate() {
        val firstMutable = first.toMutableList()
        nng.mutate(firstMutable)
        assertNotEquals(firstMutable, first, "Mutate directly should modify the list")
        val diff = List(first.size, { first[it] - firstMutable[it] })
        assertEquals(nng.mutationsPerList, diff.sumBy { v -> if (v == 0.0) 0 else 1 }, "mutate should change exactly ${nng.mutationsPerList} items")
    }

    fun fillPopulation() {
        nng.populationMap.apply {
            put(List<Double>(6, { 2.0 }), 2.0)
            put(second.toList(), 3.0)
            put(first.toList(), 3.0)
            put(first.toList(), 4.0)
            assertEquals(first.toList(), maxBy { it.value }!!.key, "MaxEntry should be list of 3s")
        }
    }

    @Test
    fun updateGeneration() {
        nng.clearFile(nng.bestFile)
        fillPopulation()
        nng.updateGeneration()
        val best = Triple(first.toList(), 0, 4.0)
        assertEquals(best, nng.getLastBest())
        assertEquals(0, nng.populationMap.size, "Population map should be cleared after updateGeneration")
        assertTrue(nng.populationSize <= nng.readPopulation().size, "population file should now hold at least ${nng.populationSize} items")
    }

    @Test
    fun listString() {
        val list = listOf(2.0, 1.0, 3.0, 5.0)
        assertEquals(list, nng.stringToList(nng.listToString(list)), "List String conversion should be reversible")
    }
}