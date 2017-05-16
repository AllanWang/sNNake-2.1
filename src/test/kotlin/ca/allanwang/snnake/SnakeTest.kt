package ca.allanwang.snnake

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
    var id: SnakeId = SnakeId._1
    var prevHeadValue: Int = 0
    val contract = object : SnakeGameContract {
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
        map = Array(gameHeight, { IntArray(gameWidth, { _ -> MapData.EMPTY.ordinal }) })
    }

    @Test
    fun human() {
        assertTrue(snake.human, "Snake should be a human")
        snake.human = false
        assertFalse(snake.human, "Snake should be a NN")
    }
}