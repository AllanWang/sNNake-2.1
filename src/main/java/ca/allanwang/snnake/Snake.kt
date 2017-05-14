package ca.allanwang.snnake

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
        val values: Array<SnakeId> = SnakeId.values() // cache array data so it doesn't get recreated each time
        fun get(i: Int): SnakeId {
            var ii = i
            if (ii >= values.size) ii %= values.size
            if (ii < 0) ii += values.size
            return values[ii]
        }
    }
}

class Snake(val id: SnakeId, val gameContract: SnakeGameContract) {

    val positions = SnakeQueue()
    var score = 0
    private var prevScore = 0
    private val map = gameContract.getMap()
    var dead = false
    private var prevHeadValue = 0
    private var prevDirection = Directions.NONE
    private var pendingDirection = id.initDirection
    private var keyEventHandler: EventHandler<KeyEvent>? = null

    fun addKeyEventHandler() {
        if (keyEventHandler == null)
            keyEventHandler = EventHandler {
                event ->
                val potentialDirection = when (event.code) {
                    id.left -> Directions.LEFT
                    id.up -> Directions.UP
                    id.right -> Directions.RIGHT
                    id.down -> Directions.DOWN
                    else -> Directions.NONE
                }
                if (potentialDirection != Directions.NONE && !prevDirection.isOpposite(potentialDirection))
                    pendingDirection = potentialDirection
            }
        if (!gameContract.getKeyListeners().contains(keyEventHandler!!))
            gameContract.getKeyListeners().add(keyEventHandler!!)
    }

    fun removeKeyEventHandler() {
        if (keyEventHandler != null) {
            gameContract.getKeyListeners().remove(keyEventHandler!!)
            keyEventHandler = null
        }
    }

    fun score(points: FlagScore) {
        score += points.points
        positions.incrementMaxSize(points.sizeIncrease)
    }

    fun step(stage: StepStage) {
        when (stage) {
            StepStage.TAIL -> {
                if (positions.size >= positions.maxSize())
                    positions.remove().set(map, MapData.EMPTY.ordinal)
            }
            StepStage.HEAD -> {
                prevDirection = pendingDirection
                positions.head.set(map, MapData.SNAKE_BODY.bind(id)) // prev head is now body
                prevHeadValue = positions.move(pendingDirection).get(map)
                val prevHeadMapData = MapData.get(prevHeadValue)
                if (prevHeadMapData != MapData.SNAKE_BODY && prevHeadMapData != MapData.SNAKE_HEAD)
                    positions.head.set(map, MapData.SNAKE_HEAD.bind(id)) // did not hit another snake; set head color
            }
            StepStage.STATUS -> {
                gameContract.sendSnakeStatus(id, prevHeadValue)
            }
        }
    }

    fun terminate() {
        dead = true
        positions.clear()
        removeKeyEventHandler()
    }

}