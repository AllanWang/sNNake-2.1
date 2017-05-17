package ca.allanwang.snnake

import org.junit.Before

/**
 * Created by Allan Wang on 2017-05-16.
 */
class NeuralNetTest {

    lateinit var net: NeuralNet

    @Before
    fun init() {
        net = NeuralNet(2, 3, 1,
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0,
                -2.0,
                3.0,
                -4.0)
    }



//    @Test
//    fun propagateMax() {
//        val actual = net.forwardPropagate(Matrix(4, 2, Double.MAX_VALUE))
//        val expected = Matrix(4, 1,Double.MAX_VALUE)
//        assertEquals(expected, actual, "Propagating max value matrix should result in a max value matrix")
//    }

}
