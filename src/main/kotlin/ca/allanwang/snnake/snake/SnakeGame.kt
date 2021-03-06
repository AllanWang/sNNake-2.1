package ca.allanwang.snnake.snake

import ca.allanwang.snnake.gameHeight
import ca.allanwang.snnake.gameWidth
import ca.allanwang.snnake.neuralnet.NNGenetics
import ca.allanwang.snnake.neuralnet.NeuralNet
import ca.allanwang.snnake.snakeVision
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import tornadofx.*


/**
 * Created by Allan Wang on 2017-05-13.
 *
 * Holds the game logic and handles the neural net
 */

/**
 * Contract that the Game maintains to each snake
 */
interface SnakeGameContract {

    fun getMap(): Array<IntArray>

    fun getApples(): List<C>

    fun getNode(): Node?

    fun sendSnakeStatus(id: SnakeId, prevHeadValue: Int)

    fun getNeuralNet(): NeuralNet
}

enum class StepStage {
    TAIL, HEAD, STATUS;

    companion object {
        val values = StepStage.values()
    }
}

class SnakeGame : Controller(), SnakeGameContract {

    override fun getNeuralNet(): NeuralNet = nng.net

    override fun getApples(): List<C> = apples

    override fun getMap(): Array<IntArray> = gameMap

    override fun getNode(): Node = snakeFrame.grid

    /**
     * At the last stage of step, allows snakes to send their previous head status so their scores may be updated
     * [id] snake sender
     * [prevHeadValue] prev map value at the location of their current head
     */
    override fun sendSnakeStatus(id: SnakeId, prevHeadValue: Int) {
        when (MapData.get(prevHeadValue)) {
            MapData.EMPTY -> return
            MapData.APPLE -> {
                score(id.ordinal, FlagScore.APPLE)
                applesToSpawn++
            }
            MapData.SNAKE_BODY -> {
                snakes[id.ordinal].terminate()
                val otherSnake = MapData.getSnake(prevHeadValue)
                if (otherSnake != id)
                    score(otherSnake.ordinal, FlagScore.CAPTURED_SNAKE)
            }
            MapData.SNAKE_HEAD -> {
                snakes[id.ordinal].terminate()
                val otherSnake = MapData.getSnake(prevHeadValue)
                snakes[otherSnake.ordinal].terminate()
            }
            MapData.INVALID -> {
                snakes[id.ordinal].terminate()
            }
        }
    }

    fun score(id: Int, score: FlagScore) {
        snakes[id].score(score, stepsWithoutProgress.toDouble() / stepsCap.toDouble())
        stepsWithoutProgress = 0
    }

    lateinit var snakeFrame: SnakeView
    private var timeline = Timeline()
    var fps = fpsDefault
        set(value) {
            field = value
            updateTimer(true)
        }
    private var pause = true
    private var gameCont = false
    var snakes = mutableListOf<Snake>()
    private var applesToSpawn: Int = 1
    private var stepsWithoutProgress: Int = 0
    private val stepsCap: Int = (gameWidth + gameHeight) * 5
    val gameMap = Array(gameHeight, { IntArray(gameWidth, { MapData.EMPTY.ordinal }) })
    val sVision = snakeVision
    val nng = NNGenetics(sVision.key, sVision.neuralNet)
    private val apples = mutableListOf<C>()

    /**
     * Called from [SnakeView] when the UI is ready
     */
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
                            else -> consume = false
                        }
                    } else consume = false
                }
            }
            if (consume) event.consume()
        })
        nng.generationCallback = generationUpdate
    }

    /**
     * Callback for [NNGenetics] to update the UI when the generation increments
     */
    val generationUpdate: (Int, List<Double>, Double) -> Unit = {
        generation, _, fitness ->
        snakeFrame.generation.text = "Generation $generation"
        snakeFrame.fitness.text = String.format("Max Fitness: %.8f", fitness)
    }

    /**
     * Binds playButton function and updates its text
     */
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
                println("Unknown playButton text $text")
                return "Error"
            }
        }
    }

    /**
     * Updates the timer based on the following changes:
     * Play/pause
     * Fps change
     * Game over
     * [recreate] forces a new timeline creation, usually due to a new fps
     */
    fun updateTimer(recreate: Boolean = false) {
        if (!pause) snakeFrame.grid.requestFocus()
        if (recreate) {
            if (timeline.status == Animation.Status.RUNNING) timeline.stop()
            timeline = Timeline(KeyFrame(Duration.millis(1000 / fps), EventHandler<ActionEvent> { playTurn() }))
            timeline.cycleCount = Animation.INDEFINITE
            if (!pause) timeline.playFromStart()
        } else if (pause && timeline.status == Animation.Status.RUNNING) timeline.stop()
        else if (!pause && timeline.status != Animation.Status.RUNNING) timeline.playFromStart()
    }

    /**
     * Checks if player is a human
     */
    fun isHuman(group: ToggleGroup): Boolean = (group.selectedToggle as RadioButton).text == HUMAN

    /**
     * After [stepsCap], makes sure that if there are only neural nets, they are making progress
     * Otherwise, restart the game to avoid infinite loops
     */
    fun checkForProgress() {
        if (!gameCont || pause) return
        if (snakes.any { s -> !s.dead && s.human }) {
            stepsWithoutProgress = 0
            return
        }
        nng.setFitnessOfCurrent((snakes.sumByDouble { s -> s.score } / snakes.size))
        newGame()
    }

    /**
     * Creates a new game with new snakes
     */
    fun newGame() {
        gameCont = true
        pause = false
        updateTimer(true)
        stepsWithoutProgress = 0
        snakes.forEach(Snake::terminate)
        snakes.clear()
        snakes.add(Snake(SnakeId._1, isHuman(snakeFrame.player1), this, sVision))
        if ((snakeFrame.player2.selectedToggle as RadioButton).text != NONE)
            snakes.add(Snake(SnakeId._2, isHuman(snakeFrame.player2), this, sVision))
        gameMap.forEachIndexed { y, row -> row.forEachIndexed { x, _ -> gameMap[y][x] = MapData.EMPTY.ordinal } }
        applesToSpawn = snakes.size
        spawnApples()
    }

    /**
     * While we still need more apples, make new apples in empty map spaces
     */
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
     *
     * @param c apple position
     *
     * @return true if valid, false otherwise
     */
    private fun validateApple(c: C): Boolean {
        if (gameMap[c.y][c.x] != MapData.EMPTY.ordinal) return false
        return snakes.none { !it.dead && c.isWithin(it.head(), 3) }
    }

    private fun randomC(): C {
        val x = (Math.random() * gameWidth).toInt()
        val y = (Math.random() * gameHeight).toInt()
        return C(x, y)
    }

    /**
     * Triggers every snake to make their next move
     */
    fun playTurn() {
        if (!gameCont) {
            timeline.stop()
            gameEnded()
            return
        }
        if (stepsWithoutProgress > stepsCap) checkForProgress()
        StepStage.values.forEach { stage -> snakes.forEach { it.step(stage) } }
        spawnApples()
        stepsWithoutProgress++
        draw(gameMap)
        gameCont = snakes.any { !it.dead }
    }

    /**
     * Marks the end of the game; all snakes are dead
     */
    fun gameEnded() {
        nng.setFitnessOfCurrent((snakes.sumByDouble(Snake::score) / snakes.size))
        newGame()
    }

    /**
     * Redraws the entire grid using [map] values
     */
    fun draw(map: Array<IntArray>) {
        apples.clear()
        snakeFrame.grid.children.filter { it is Rectangle }.forEach {
            rect ->
            val row = GridPane.getRowIndex(rect)
            val col = GridPane.getColumnIndex(rect)
            val data = MapData.get(map[row][col])
            if (data == MapData.SNAKE_HEAD || data == MapData.SNAKE_BODY) {
                if (snakes[MapData.getSnake(map[row][col]).ordinal].dead)
                    map[row][col] = MapData.EMPTY.ordinal
            } else if (data == MapData.APPLE)
                apples.add(C(row, col))
            MapData.color(rect as Rectangle, map[row][col])
        }
    }
}
