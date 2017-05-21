package ca.allanwang.snnake.snake

import ca.allanwang.snnake.neuralnet.Matrix
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 2017-05-20.
 */
class SnakeVisionTest {

    lateinit var map: Array<IntArray>
    lateinit var apples: List<C>

    @Before
    fun init() {
        map = arrayOf(
                arrayOf(MapData.EMPTY, MapData.EMPTY, MapData.APPLE, MapData.EMPTY, MapData.EMPTY, MapData.EMPTY),
                arrayOf(MapData.EMPTY, MapData.SNAKE_BODY, MapData.SNAKE_BODY, MapData.EMPTY, MapData.EMPTY, MapData.APPLE),
                arrayOf(MapData.SNAKE_BODY, MapData.SNAKE_BODY, MapData.EMPTY, MapData.EMPTY, MapData.EMPTY, MapData.EMPTY),
                arrayOf(MapData.SNAKE_BODY, MapData.EMPTY, MapData.EMPTY, MapData.EMPTY, MapData.EMPTY, MapData.EMPTY),
                arrayOf(MapData.SNAKE_BODY, MapData.SNAKE_BODY, MapData.SNAKE_BODY, MapData.EMPTY, MapData.EMPTY)
        ).map { it.map { it.ordinal }.toIntArray() }.toTypedArray()
        apples = listOf(C(2, 0), C(5, 2))
    }

    fun verifyInputMatrix(vision: SnakeVision, head: C, prevDirection: Directions, key: String, result: Matrix) {
        val inputMatrix = vision.getInputMatrix(map, head, prevDirection, apples, 5)
        assertEquals(result, inputMatrix, "Input Matrix V${vision.name}_${prevDirection}_$key failed")
    }

    @Test
    fun inputMatrix1ZeroUp() {
        val result = Matrix(1, 6, 0.0, 0.0, 5.0 / 6.0, 2.0 / 6.0, 0.0, 5.0 / 30.0)
        verifyInputMatrix(SnakeVision._1, C(0, 0), Directions.UP, "zero", result)
    }

    @Test
    fun inputMatrix1ZeroLeft() {
        val result = Matrix(1, 6, 1.0 / 5.0, 0.0, 0.0, 0.0, -2.0 / 6.0, 5.0 / 30.0)
        verifyInputMatrix(SnakeVision._1, C(0, 0), Directions.LEFT, "zero", result)
    }

    @Test
    fun inputMatrix1CenterRight() {
        val result = Matrix(1, 6, 0.0, 3.0 / 6.0, 1.0 / 5.0, -2.0 / 5.0, 0.0, 5.0 / 30.0)
        verifyInputMatrix(SnakeVision._1, C(2, 2), Directions.RIGHT, "2", result)
    }

    @Test
    fun inputMatrix2ZeroUp() {
        val head = C(0, 0)
        val apple = (head closest apples)!!
        val result = Matrix(3, 5,
                -1.0,-1.0, -1.0, -1.0, head.delta(apple, -1, 0),
                -1.0,-1.0, -1.0, -1.0, head.delta(apple, 0, 1),
                0.0,-1.0, 1.0, -1.0, head.delta(apple, 1, 0))
        verifyInputMatrix(SnakeVision._2, head, Directions.UP, "zero", result)
    }

    @Test
    fun inputMatrix2CenterRight() {
        val head = C(2, 2)
        val apple = (head closest apples)!!
        val result = Matrix(3, 5,
                -1.0,-1.0, 1.0, 0.0, head.delta(apple, 0, 1),
                0.0,0.0, 0.0, 0.0, head.delta(apple, 1, 0),
                0.0,0.0, -1.0, 0.0, head.delta(apple, 0, -1))
        verifyInputMatrix(SnakeVision._2, head, Directions.RIGHT, "2", result)
    }
}