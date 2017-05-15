package ca.allanwang.snnake

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import tornadofx.*


/**
 * Created by Allan Wang on 2017-05-13.
 */

/**
 * Contract that the Game maintains to each snake
 */
interface SnakeGameContract {

    fun getMap(): Array<IntArray>

    fun getNode(): Node

    fun sendSnakeStatus(id: SnakeId, prevHeadValue: Int)
}

enum class StepStage {
    TAIL, HEAD, STATUS;

    companion object {
        val values = StepStage.values()
    }
}

class SnakeGame : Controller(), SnakeGameContract {

    override fun getMap(): Array<IntArray> = gameMap

    override fun getNode(): Node = snakeFrame.grid

    override fun sendSnakeStatus(id: SnakeId, prevHeadValue: Int) {
        when (prevHeadValue % 10) {
            MapData.EMPTY.ordinal -> return
            MapData.APPLE.ordinal -> {
                snakes[id.ordinal]?.score(FlagScore.APPLE)
                applesToSpawn++
            }
            MapData.SNAKE_BODY.ordinal -> {
                snakes[id.ordinal]?.terminate()
                val otherSnake = MapData.getSnake(prevHeadValue)
                if (otherSnake != id)
                    snakes[otherSnake.ordinal]?.score(FlagScore.CAPTURED_SNAKE)
            }
            MapData.SNAKE_HEAD.ordinal -> {
                snakes[id.ordinal]?.terminate()
                val otherSnake = MapData.getSnake(prevHeadValue)
                snakes[otherSnake.ordinal]?.terminate()
            }
            MapData.INVALID.ordinal -> {
                snakes[id.ordinal]?.terminate()
            }
            else -> {
                println(String.format("Unknown status %d from snake %d", prevHeadValue, id.ordinal))
            }
        }
    }

    lateinit var snakeFrame: SnakeView
    private var timeline = Timeline()
    var fps = fpsDefault
    private var lastScore = 0
    var score = 0
    private var pause = true
    private var gameCont = false
    var snakes = arrayOfNulls<Snake>(snakeCount)
    var applesToSpawn = snakeCount
    val gameMap = Array(gameHeight, { IntArray(gameWidth, { _ -> MapData.EMPTY.ordinal }) })

    fun bind(snakeFrame: SnakeView) {
        println("Ready")
        this.snakeFrame = snakeFrame
        getNode().addEventHandler(KeyEvent.KEY_PRESSED, {
            event ->
            var consume = true
            when (event.code) {
                KeyCode.SPACE -> {
                    pause = !pause
                    updateTimer()
                    snakeFrame.play.text = if (pause) "Resume" else "Pause"
                }
                else -> {
                    if (event.isControlDown) {
                        when (event.code) {
                            KeyCode.R -> newGame()
                            else -> {
                                consume = false
                            }
                        }
                    }
                }
            }
            if (consume) event.consume()
        })
    }

    fun playButton(text: String): String {
        when (text) {
            "Start" -> {
                newGame()
                return "Pause"
            }
            "Pause" -> {
                pause = true
                updateTimer()
                return "Resume"
            }
            "Resume" -> {
                pause = false
                updateTimer()
                return "Pause"
            }
            else -> {
                println(String.format("Unknown playButton text %s", text))
                return "Error"
            }
        }
    }

    fun changeFps(fps: Double) {
        this.fps = fps
        updateTimer(true)
    }

    fun updateTimer(recreate: Boolean = false) {
        if (!pause) snakeFrame.grid.requestFocus()
        if (recreate) {
            if (timeline.status == Animation.Status.RUNNING) timeline.stop()
            timeline = Timeline(KeyFrame(Duration.millis(1000 / fps), EventHandler<ActionEvent> {
                playTurn()
            }))
            timeline.cycleCount = Animation.INDEFINITE
            if (!pause) timeline.playFromStart()
        } else if (pause && timeline.status == Animation.Status.RUNNING) timeline.stop()
        else if (!pause && timeline.status != Animation.Status.RUNNING) timeline.playFromStart()
    }

    fun newGame() {
        if (snakeCount > 4) throw IllegalArgumentException("We can have at most 4 snakes")
        gameCont = true
        pause = false
        updateTimer(true)
        snakes.forEachIndexed {
            i, s ->
            s?.terminate()
            snakes[i] = Snake(SnakeId.get(i), this)
            gameMap.forEachIndexed { y, row ->
                row.forEachIndexed {
                    x, _ ->
                    gameMap[y][x] = MapData.EMPTY.ordinal
                }
            }
        }
        applesToSpawn = snakeCount
        spawnApples()
    }

    private fun spawnApples() {
        while (applesToSpawn > 0) {
            val (x, y) = spawnApple()
            gameMap[y][x] = MapData.APPLE.ordinal
            applesToSpawn--
        }
    }

    private fun spawnApple(): C {
        var c: C
        do {
            c = randomC()
        } while (!validateApple(c))
        return c
    }

    /**
     * Apple should not generate within 3 dp from a snake head and should not rest on the snake's body
     * Apple should also not be on top of another existing apple

     * @param c apple position
     * *
     * @return true if valid, false otherwise
     */
    private fun validateApple(c: C): Boolean {
        if (gameMap[c.y][c.x] != MapData.EMPTY.ordinal) return false
        return snakes.none { it != null && !it.dead && c.isWithin(it.head(), 3) }
    }

    private fun randomC(): C {
        val x = (Math.random() * gameWidth).toInt()
        val y = (Math.random() * gameHeight).toInt()
        return C(x, y)
    }

    fun playTurn() {
        if (!gameCont) {
            timeline.stop()
            println("Game Over")
            snakeFrame.play.text = "Start"
            return
        }
        StepStage.values.forEach {
            stage ->
            snakes.forEach {
                snake ->
                snake?.step(stage)
            }
        }
        spawnApples()
        draw(gameMap)
        gameCont = false
        snakes.forEach {
            snake ->
            gameCont = gameCont || !(snake?.dead ?: true)
        }
    }

    fun draw(map: Array<IntArray>) {
        snakeFrame.grid.children.filter { it is Rectangle }.forEach {
            rect ->
            val row = GridPane.getRowIndex(rect)
            val col = GridPane.getColumnIndex(rect)
            val data = MapData.get(map[row][col])
            if (data == MapData.SNAKE_HEAD || data == MapData.SNAKE_BODY)
                if (snakes[MapData.getSnake(map[row][col]).ordinal]?.dead ?: true)
                    map[row][col] = MapData.EMPTY.ordinal
            MapData.color(rect as Rectangle, map[row][col])
        }
    }

}
