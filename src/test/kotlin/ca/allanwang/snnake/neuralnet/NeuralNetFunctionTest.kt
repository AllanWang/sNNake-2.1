package ca.allanwang.snnake.neuralnet

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 2017-05-17.
 *
 * All functions in the NeuralNet should not modify its arguments. We will test it against deep clone to ensure that this is the case
 * How the function performs is irrelevant in these tests
 */
class NeuralNetFunctionTest {

    lateinit var net: NeuralNet
    lateinit var x: Matrix
    lateinit var xx: Matrix
    lateinit var addSub: Matrix
    lateinit var mult: Matrix
    @Before
    fun init() {
        x = Matrix(3, 2,
                3, 5,
                5, 1,
                10, 2)
        addSub = Matrix(3, 2, 75, 82, 93, 33, 12, 23)
        mult = Matrix(2, 2, 75, 82, 93, 33)
        net = NeuralNet(2, 3, 2, activator = Activator.SIGMOID, random = Random.ONE)
        xx = x.deepClone()
    }

    fun verify(function: (Matrix) -> Any) {
        function(x)
        assertEquals(xx, x, "$function: Matrix x was modified during function call")
    }

    fun verify(function: (Matrix, Matrix) -> Any, y: Matrix) {
        val yy = y.deepClone()
        function(x, y)
        assertEquals(xx, x, "$function: Matrix x was modified during function call")
        assertEquals(yy, y, "$function: Matrix y was modified during function call")
    }

    @Test
    fun forward() = verify({ x -> net.forward(x) })

    @Test
    fun output() = verify({ x -> net.output(x) })

    @Test
    fun costFunction() = verify({ x, y -> net.costFunction(x, y) }, addSub)

    @Test
    fun costFunctionPrime() = verify({ x, y -> net.costFunctionPrime(x, y) }, addSub)
}