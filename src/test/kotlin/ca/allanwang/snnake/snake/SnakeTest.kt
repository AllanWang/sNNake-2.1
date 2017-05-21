package ca.allanwang.snnake.snake

import ca.allanwang.snnake.gameHeight
import ca.allanwang.snnake.gameWidth
import ca.allanwang.snnake.neuralnet.NeuralNet
import ca.allanwang.snnake.snake.MapData.EMPTY
import javafx.scene.Node
import org.junit.Before
import org.junit.Test
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

        override fun getNeuralNet(): NeuralNet = throw RuntimeException("Neural Net not implemented in tests")

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
        snake = Snake(SnakeId._1, true, contract, SnakeVision._1)
        map = Array(gameHeight, { IntArray(gameWidth, { EMPTY.ordinal }) })
        appleList = mutableListOf(C(0, 0))
    }

    @Test
    fun human() {
        assertTrue(snake.human, "Snake should be a human")
        snake.human = false
        assertFalse(snake.human, "Snake should be a NN")
    }
}