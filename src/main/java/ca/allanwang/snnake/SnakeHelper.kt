package ca.allanwang.snnake

import javafx.scene.shape.Rectangle
import java.util.*

/**
 * Created by Allan Wang on 2017-05-13.
 */
//dimensions
const val gameHeight: Int = 20
const val gameWidth: Int = 40
const val blockSize = 20.0
const val snakeDefaultSize: Int = 3
val initOffset: Int = Math.min(gameHeight / 5, gameWidth / 5)

enum class Directions {
    NONE {
        override fun apply(c: C): C = c
        override fun isOpposite(d: Directions) = false
    },
    UP {
        override fun apply(c: C): C = C(c.x, c.y - 1)
        override fun isOpposite(d: Directions) = d == DOWN
    },
    RIGHT {
        override fun apply(c: C): C = C(c.x + 1, c.y)
        override fun isOpposite(d: Directions) = d == LEFT
    },
    DOWN {
        override fun apply(c: C): C = C(c.x, c.y + 1)
        override fun isOpposite(d: Directions) = d == UP
    },
    LEFT {
        override fun apply(c: C): C = C(c.x - 1, c.y)
        override fun isOpposite(d: Directions) = d == RIGHT
    };

    abstract fun apply(c: C): C
    abstract fun isOpposite(d: Directions): Boolean
}

//maximum of 9 values; snake id (if it exists) will by multiplied by 10 and added to the ordinal
enum class MapData {
    INVALID {
        override fun color(rect: Rectangle, id: SnakeId) {
            rect.fill = SnakeStyle.background
        }
    },
    EMPTY {
        override fun color(rect: Rectangle, id: SnakeId) {
            rect.fill = SnakeStyle.background
        }
    },
    APPLE {
        override fun color(rect: Rectangle, id: SnakeId) {
            rect.fill = SnakeStyle.apple
        }
    },
    SNAKE_BODY {
        override fun color(rect: Rectangle, id: SnakeId) {
            rect.fill = id.color.darker()
        }
    },
    SNAKE_HEAD {
        override fun color(rect: Rectangle, id: SnakeId) {
            rect.fill = id.color
        }
    };

    fun bind(i: SnakeId): Int = 10 * i.ordinal + ordinal

    abstract fun color(rect: Rectangle, id: SnakeId)

    companion object {
        val values: Array<MapData> = MapData.values() // cache array data so it doesn't get recreated each time
        fun get(i: Int): MapData {
            var ii = i
            if (ii >= values.size) ii %= values.size
            if (ii < 0) ii += values.size
            return values[ii]
        }

        fun getSnake(i: Int): SnakeId = SnakeId.get(i / 10)
        fun color(rect: Rectangle, i: Int) {
            get(i).color(rect, getSnake(i))
        }
    }
}

enum class FlagScore(val points: Int, val sizeIncrease: Int) {
    APPLE(1, 1), CAPTURED_SNAKE(10, 5)
}

enum class FlagTerminate {
    WALL, RESET
}

data class C(val x: Int, val y: Int) {
    fun isWithin(c: C, proximity: Int): Boolean = Math.abs(c.x - x) < proximity && Math.abs(c.y - y) < proximity
    fun set(map: Array<IntArray>, toSet: Int): Int {
        val original = get(map)
        if (original == MapData.INVALID.ordinal) return MapData.INVALID.ordinal
        map[y][x] = toSet
        // otherwise keep original snake body
        return original
    }

    fun get(map: Array<IntArray>): Int {
        if (x < 0 || y < 0 || y >= map.size || x >= map[0].size)
            return MapData.INVALID.ordinal
        return map[y][x]
    }
}

class SnakeQueue(private var maxSize: Int = snakeDefaultSize) : LinkedList<C>() {

    init {
        if (maxSize < 1) throw IllegalArgumentException("Max size must be at least 1")
    }

    override fun add(element: C): Boolean {
        addFirst(element)
        return true
    }

    override fun remove(): C {
        return super.removeLast()
    }

    val head: C
        get() = first

    fun move(direction: Directions): C {
        add(direction.apply(head))
        return head
    }

    override fun addFirst(c: C) {
        super.addFirst(c)
        trim()
    }

    override fun addLast(c: C) {
        super.addLast(c)
        trim()
    }

    fun setMaxSize(maxSize: Int) {
        this.maxSize = maxSize
        trim()
    }

    @JvmOverloads fun incrementMaxSize(i: Int = 1) {
        setMaxSize(maxSize() + i)
    }

    fun maxSize(): Int = maxSize

    private fun trim() {
        while (size > maxSize)
            removeLast()
    }
}