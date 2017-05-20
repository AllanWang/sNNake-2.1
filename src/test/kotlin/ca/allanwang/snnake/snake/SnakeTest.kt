package ca.allanwang.snnake.snake

import ca.allanwang.snnake.gameHeight
import ca.allanwang.snnake.gameWidth
import ca.allanwang.snnake.neuralnet.Matrix
import ca.allanwang.snnake.snake.MapData.*
import javafx.scene.Node
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 2017-05-15.
 */
class SnakeTest {

    lateinit var snake: Snake
    var map: Array<IntArray> = emptyArray<IntArray>()
    var appleList: List<C> = mutableListOf()
    var id: SnakeId = SnakeId._1
    var prevHeadValue: Int = 0
    val contract = object : SnakeGameContract {

        override fun getNeuralOutput(input: Matrix): Matrix = throw RuntimeException("Neural Net not implemented in tests")

        override fun getApples() = appleList

        override fun getMap() = map

        override fun getNode(): Node? = null

        override fun sendSnakeStatus(id: SnakeId, prevHeadValue: Int) {
            this@SnakeTest.id = id
            this@SnakeTest.prevHeadValue = prevHeadValue
        }
    }

    @Before
    fun init() {
        snake = Snake(SnakeId._1, true, contract)
        map = Array(gameHeight, { IntArray(gameWidth, { EMPTY.ordinal }) })
        appleList = mutableListOf(C(0, 0))
    }

    @Test
    fun human() {
        assertTrue(snake.human, "Snake should be a human")
        snake.human = false
        assertFalse(snake.human, "Snake should be a NN")
    }

    fun getInputMatrix(head: C, prevDirection: Directions): Matrix {
        val newMap = arrayOf(
                arrayOf(EMPTY, EMPTY, APPLE, EMPTY, EMPTY, EMPTY),
                arrayOf(EMPTY, SNAKE_BODY, SNAKE_BODY, EMPTY, EMPTY, APPLE),
                arrayOf(SNAKE_BODY, SNAKE_BODY, EMPTY, EMPTY, EMPTY, EMPTY),
                arrayOf(SNAKE_BODY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY),
                arrayOf(SNAKE_BODY, SNAKE_BODY, SNAKE_BODY, EMPTY, EMPTY)
        ).map { it.map { it.ordinal }.toIntArray() }.toTypedArray()
        val apples = listOf(C(2, 0), C(5, 2))
        return snake.getInputMatrix(newMap, head, prevDirection, apples, 5)
    }

    @Test
    fun inputMatrixZeroUp() {
        val result = Matrix(1, 6, 0.0, 0.0, 5.0 / 6.0, 2.0 / 6.0, 0.0, 5.0 / 30.0)
        assertEquals(result, getInputMatrix(C(0, 0), Directions.UP), "Input Matrix UP failed")
    }

    @Test
    fun inputMatrixZeroLeft() {
        val result = Matrix(1, 6, 1.0 / 5.0, 0.0, 0.0, 0.0, -2.0 / 6.0, 5.0 / 30.0)
        assertEquals(result, getInputMatrix(C(0, 0), Directions.LEFT), "Input Matrix LEFT failed")
    }

    @Test
    fun inputMatrixCenterRight() {
        val result = Matrix(1, 6, 0.0, 3.0 / 6.0, 1.0 / 5.0, -2.0 / 5.0, 0.0, 5.0 / 30.0)
        assertEquals(result, getInputMatrix(C(2, 2), Directions.RIGHT), "Input Matrix RIGHT failed")
    }
}