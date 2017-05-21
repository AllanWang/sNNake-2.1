package ca.allanwang.snnake.snake

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 2017-05-20.
 */
class MapDataTest {

    @Test
    fun binding() {
        val i = MapData.SNAKE_HEAD.bind(SnakeId._2)
        assertEquals(MapData.SNAKE_HEAD, MapData.get(i))
        assertEquals(SnakeId._2, MapData.getSnake(i))
    }
}