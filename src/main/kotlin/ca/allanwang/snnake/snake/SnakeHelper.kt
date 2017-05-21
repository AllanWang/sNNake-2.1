package ca.allanwang.snnake.snake

import ca.allanwang.snnake.snakeDefaultSize
import javafx.scene.shape.Rectangle
import java.util.*

/**
 * Created by Allan Wang on 2017-05-13.
 */

enum class Directions(val northShift: Int) {
    NONE(0) {
        override fun apply(c: C): C = c
        override fun isOpposite(d: Directions) = false
        override val left = lazy { NONE }
        override val right = lazy { NONE }
        override fun relativeAxis(up: Double, right: Double): Pair<Double, Double> = Pair(up, right)
    },
    UP(0) {
        override fun apply(c: C): C = C(c.x, c.y - 1)
        override fun isOpposite(d: Directions) = d == DOWN
        override val left = lazy { LEFT }
        override val right = lazy { RIGHT }
        override fun relativeAxis(up: Double, right: Double): Pair<Double, Double> = Pair(up, right)
    },
    RIGHT(3) {
        override fun apply(c: C): C = C(c.x + 1, c.y)
        override fun isOpposite(d: Directions) = d == LEFT
        override val left = lazy { UP }
        override val right = lazy { DOWN }
        override fun relativeAxis(up: Double, right: Double): Pair<Double, Double> = Pair(right, -up)
    },
    DOWN(2) {
        override fun apply(c: C): C = C(c.x, c.y + 1)
        override fun isOpposite(d: Directions) = d == UP
        override val left = lazy { RIGHT }
        override val right = lazy { LEFT }
        override fun relativeAxis(up: Double, right: Double): Pair<Double, Double> = Pair(-up, -right)
    },
    LEFT(1) {
        override fun apply(c: C): C = C(c.x - 1, c.y)
        override fun isOpposite(d: Directions) = d == RIGHT
        override val left = lazy { DOWN }
        override val right = lazy { UP }
        override fun relativeAxis(up: Double, right: Double): Pair<Double, Double> = Pair(-right, up)
    };

    abstract fun apply(c: C): C
    abstract fun isOpposite(d: Directions): Boolean
    fun relativeIndex(i: Int, size: Int): Int {
        var index = (i + northShift) % size
        while (index < 0) index += size
        return index
    }

    abstract val left: Lazy<Directions>
    abstract val right: Lazy<Directions>
    abstract fun relativeAxis(up: Double, right: Double): Pair<Double, Double>
}

//maximum of 9 values; snake id (if it exists) will by multiplied by 10 and added to the ordinal
enum class MapData {
    EMPTY {
        override fun color(rect: Rectangle, id: SnakeId) {
            rect.fill = SnakeStyle.background
        }
    },
    INVALID {
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
            var ii = i % 10
            if (ii < 0) ii += 10
            return values[ii]
        }

        fun getSnake(i: Int): SnakeId = SnakeId.get(i / 10)
        fun color(rect: Rectangle, i: Int) {
            get(i).color(rect, getSnake(i))
        }
    }
}

enum class FlagScore(val points: Double, val sizeIncrease: Int) {
    APPLE(1.0, 1), CAPTURED_SNAKE(10.0, 5)
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

infix fun C.distanceTo(c: C): Double = Math.hypot((x - c.x).toDouble(), (y - c.y).toDouble())

infix fun C.closest(points: List<C>): C? = points.minBy { c -> this distanceTo c }

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