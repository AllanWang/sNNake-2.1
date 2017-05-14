package ca.allanwang.snnake

import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import tornadofx.*


/**
 * Created by Allan Wang on 2017-05-13.
 */

/**
 * Contract that the Game maintains to each snake
 */
interface SnakeGameContract {

    fun getKeyListeners(): MutableList<EventHandler<KeyEvent>>

    fun getMap(): Array<IntArray>

    fun sendSnakeStatus(id: SnakeId, prevHeadValue: Int)
}

enum class StepStage {
    TAIL, HEAD, STATUS
}

class SnakeGame : Controller() {
    val snakeFrame: SnakeView by inject()
    var monitor = Timeline()
    var fps = fpsDefault
    var lastScore = 0
    var score = 0
    var pause = true

    fun init() {
        with(snakeFrame as ViewContract) {
            //            draw()
        }
    }

    fun playPause(): String {
        pause = !pause
        return if (pause) "Start" else "Pause"
    }

    fun changeFps(fps: Double) {
        this.fps = fps
    }

//    fun initMonitor() {
//        monitor.stop()
//        monitor = Timeline(KeyFrame(Duration.millis(100000 / fps), { ae ->
//            if (lastScore >= score) {
//                trainer.updateFitnessOfCurrent(score)
//                loadStart()
//            }
//            lastScore = score
//        }))
//        monitor.cycleCount = Animation.INDEFINITE
//        if (button.getText().equals("Pause") && currentPlayer.equals("NN Training"))
//            monitor.play()
//    }

}
