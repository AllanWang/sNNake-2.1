package ca.allanwang.snnake.snake

import ca.allanwang.snnake.gameHeight
import ca.allanwang.snnake.gameWidth
import ca.allanwang.snnake.initOffset
import ca.allanwang.snnake.neuralnet.Matrix
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color

/**
 * Created by Allan Wang on 2017-05-14.
 */
enum class SnakeId(val color: Color, val left: KeyCode, val up: KeyCode, val right: KeyCode, val down: KeyCode, val initDirection: Directions, val initX: Int, val initY: Int) {
    _1(Color.GREEN, KeyCode.A, KeyCode.W, KeyCode.D, KeyCode.S, Directions.RIGHT, initOffset, initOffset),
    _2(Color.CYAN, KeyCode.LEFT, KeyCode.UP, KeyCode.RIGHT, KeyCode.DOWN, Directions.LEFT, gameWidth - initOffset, gameHeight - initOffset);

    companion object {
        val values: Array<SnakeId> = values() // cache array data so it doesn't random recreated each time
        fun get(i: Int): SnakeId {
            var ii = i
            if (ii >= values.size) ii %= values.size
            if (ii < 0) ii += values.size
            return values[ii]
        }
    }
}

class Snake(val id: SnakeId, human: Boolean, val gameContract: SnakeGameContract) {
    var human = human
        set(value) {
            field = value
            if (field) addKeyEventHandler()
            else removeKeyEventHandler()
        }
    val positions = SnakeQueue()
    var score = 0.0
    private var prevScore = 0.0
    private val map = gameContract.getMap()
    var dead = false
    private var prevHeadValue = 0
    private var prevDirection = Directions.NONE
    private var pendingDirection = id.initDirection
    private var keyEventHandler: EventHandler<KeyEvent>? = null

    fun head() = positions.head

    init {
        positions.add(C(id.initX, id.initY))
        this.human = human
    }

    fun addKeyEventHandler() {
        val grid = gameContract.getNode()
        if (grid == null || keyEventHandler != null) return
        keyEventHandler = EventHandler<KeyEvent> {
            event ->
            val potentialDirection = when (event.code) {
                id.left -> Directions.LEFT
                id.up -> Directions.UP
                id.right -> Directions.RIGHT
                id.down -> Directions.DOWN
                else -> Directions.NONE
            }
            if (potentialDirection != Directions.NONE) {
                event.consume()
                if (!prevDirection.isOpposite(potentialDirection))
                    pendingDirection = potentialDirection
            }
        }
        grid.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler)
    }

    fun removeKeyEventHandler() {
        val grid = gameContract.getNode()
        if (grid == null || keyEventHandler == null) return
        grid.removeEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler)
        keyEventHandler = null
    }

    fun score(points: FlagScore) {
        score += points.points
        positions.incrementMaxSize(points.sizeIncrease)
    }

    fun flushScore() {
        prevScore = score
    }

    fun hasScoreChanged() = prevScore < score

    fun step(stage: StepStage) {
        if (dead) return
        when (stage) {
            StepStage.TAIL -> {
                if (positions.size >= positions.maxSize())
                    positions.remove().set(map, MapData.EMPTY.ordinal)
            }
            StepStage.HEAD -> {
                if (!human) pendingDirection = getNextDirection()
                prevDirection = pendingDirection
                head().set(map, MapData.SNAKE_BODY.bind(id)) // prev head is now body
                prevHeadValue = positions.move(pendingDirection).get(map)
                val prevHeadMapData = MapData.get(prevHeadValue)
                if (prevHeadMapData != MapData.SNAKE_BODY && prevHeadMapData != MapData.SNAKE_HEAD)
                    head().set(map, MapData.SNAKE_HEAD.bind(id)) // did not hit another snake; set head color
            }
            StepStage.STATUS -> {
                gameContract.sendSnakeStatus(id, prevHeadValue)
            }
        }
    }

    /**
     * We will feed 6 inputs to the neural network to decide the best direction to go to
     * All values are found relative to North, but are given in an order relative to the Snake's current direction
     * All values are also normalized (eg # of blocks -> # of blocks/total length)
     * Input 1      blocks to left of snake until obstacle
     * Input 2      blocks in front of snake until obstacle
     * Input 3      blocks to right of snake until obstacle
     * Input x      blocks to back of snake until obstacle (this will always be 1 and will not be given to the NN
     * Input 4      horizontal block count to closest apple (right is positive)
     * Input 5      vertical block count to closest apple (up is positive)
     * Input 6      snake size/(game height * width)
     *
     * The net will return 3 outputs: (left, up, right)
     * The node with the biggest value will be the direction to go to
     */
    internal fun getInputMatrix(map: Array<IntArray> = this.map, head: C = this.head(), prevDirection: Directions = this.prevDirection, apples: List<C> = gameContract.getApples(), snakeSize: Int = this.positions.size): Matrix {
        val (x, y) = head
        val mapWidth = map[0].size
        val mapHeight = map.size

        val obstacle = DoubleArray(4)
        val left = (x - 1 downTo 0).asSequence().indexOfFirst { map[y][it] != MapData.EMPTY.ordinal && map[y][it] != MapData.APPLE.ordinal }
        obstacle[prevDirection.relativeIndex(0, 4)] = (if (left != -1) left else x).toDouble() / mapWidth

        val front = (y - 1 downTo 0).asSequence().indexOfFirst { map[it][x] != MapData.EMPTY.ordinal && map[it][x] != MapData.APPLE.ordinal }
        obstacle[prevDirection.relativeIndex(1, 4)] = (if (front != -1) front else y).toDouble() / mapHeight

        val right = (x + 1..mapWidth - 1).asSequence().indexOfFirst { map[y][it] != MapData.EMPTY.ordinal && map[y][it] != MapData.APPLE.ordinal }
        obstacle[prevDirection.relativeIndex(2, 4)] = (if (right != -1) right else mapWidth - 1 - x).toDouble() / mapWidth

        val down = (y + 1..mapHeight - 1).asSequence().indexOfFirst { map[it][x] != MapData.EMPTY.ordinal && map[it][x] != MapData.APPLE.ordinal }
        obstacle[prevDirection.relativeIndex(3, 4)] = (if (down != -1) down else mapHeight - 1 - y).toDouble() / mapHeight

        val closestApple = head closest (apples.filter { a -> a != head }) ?: head
        val input = obstacle.slice(IntRange(0, 2)).toMutableList()
        val (ay, ax) = prevDirection.relativeAxis((y - closestApple.y).toDouble() / mapHeight, (closestApple.x - x).toDouble() / mapWidth)
        input.add(ax)
        input.add(ay)
        input.add(snakeSize.toDouble() / (mapHeight * mapWidth))
        return Matrix(1, 6, input)
    }

    internal fun getNextDirection(): Directions {
        if (prevDirection == Directions.NONE) prevDirection = pendingDirection
        val output = gameContract.getNeuralOutput(getInputMatrix()).toList()
        return when (Math.max(Math.max(output[0], output[1]), output[2])) {
            output[0] -> prevDirection.left.value
            output[2] -> prevDirection.right.value
            else -> prevDirection
        }
    }

    fun terminate(stepsAlive: Long) {
//        println("Snake ${id.ordinal} died")
        score += (stepsAlive.toDouble() / 200)
        dead = true
        positions.clear()
        removeKeyEventHandler()
    }

}
