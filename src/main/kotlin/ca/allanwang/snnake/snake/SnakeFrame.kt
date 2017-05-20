package ca.allanwang.snnake.snake

import ca.allanwang.snnake.blockSize
import ca.allanwang.snnake.gameHeight
import ca.allanwang.snnake.gameWidth
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCode
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

/**
 * Created by Allan Wang on 2017-05-13.
 *
 * The UI component of the game; other constants may be found under SnakeConfig.kt
 */
const val fpsDefault: Double = 20.0
const val fpsMin: Double = 0.0
const val fpsMax: Double = 120.0
const val fpsInc: Double = 2.0

const val HUMAN = "Human"
const val NEURAL_NET = "NN"
const val NONE = "None"

class SnakeView : View("sNNake 2.1") {

    override val root = VBox()

    val controller: SnakeGame by inject()
    val player1 = ToggleGroup()
    val player2 = ToggleGroup()
    lateinit var grid: GridPane
    lateinit var play: Button
    private lateinit var fps: Label
    internal lateinit var generation: Label
    internal  lateinit var fitness: Label

    init {
        with(root) {
            borderpane {
                left = vbox {
                    alignment = Pos.TOP_CENTER
                    hbox {
                        vbox {
                            label("Player 1")
                            radiobutton(HUMAN, player1)
                            radiobutton(NEURAL_NET, player1) {
                                isSelected = true
                            }
                        }
                        vbox {
                            label("Player 2")
                            radiobutton(HUMAN, player2)
                            radiobutton(NEURAL_NET, player2)
                            radiobutton(NONE, player2) {
                                isSelected = true
                            }

                        }
                        children.filter { it is VBox }.addClass(SnakeStyle.padding)
                    }
                    slider(fpsMin, fpsMax, fpsDefault, Orientation.HORIZONTAL) {
                        addClass(SnakeStyle.paddingHorizontal)
                        blockIncrement = 6.0
                        isShowTickMarks = true
                        isShowTickLabels = true
                        majorTickUnit = 20.0
                        minorTickCount = 2
                        isSnapToTicks = true
                        valueProperty().addListener {
                            _, oldValue, newValue ->
                            val oldR = fpsInc * Math.round(oldValue.toDouble() / fpsInc)
                            val newR = fpsInc * Math.round(newValue.toDouble() / fpsInc)
                            if (oldR != newR) {
                                value = newR
                                fps.text = "${newR.toLong()} fps"
                                controller.changeFps(newR)
                            }
                        }
                    }
                    fps = label("${controller.fps.toLong()} fps") {
                        addClass(SnakeStyle.paddingBottom)
                    }
                    play = button("Start") {
                        shortcut(KeyCode.SPACE.name)
                        action {
                            text = controller.playButton(text)
                        }
                    }
                    generation = label("Generation -") {
                        addClass(SnakeStyle.paddingVertical)
                    }
                    fitness = label("Max Fitness: -")  {
                        addClass(SnakeStyle.paddingBottom)
                    }
                }
                center = vbox {
                    grid = gridpane {
                        for (y in 0..gameHeight - 1)
                            for (x in 0..gameWidth - 1)
                                rectangle {
                                    gridpaneConstraints {
                                        columnRowIndex(x, y)
                                    }
                                    width = blockSize
                                    height = blockSize
                                    fill = SnakeStyle.background
                                }
                    }
                }
            }
        }
        controller.bind(this)
    }
}

class SnakeStyle : Stylesheet() {
    companion object {
        val padding by cssclass()
        val paddingHorizontal by cssclass()
        val paddingVertical by cssclass()
        val paddingBottom by cssclass()
        val apple: Color = Color.RED
        val background: Color = Color.BLACK
    }

    init {
        padding {
            padding = box(20.px)
        }
        paddingHorizontal {
            padding = box(0.px, 20.px)
        }
        paddingVertical {
            padding = box(20.px, 0.px)
        }
        paddingBottom {
            padding = box(0.px, 0.px, 20.px, 0.px)
        }
        radioButton {
            padding = box(5.px, 0.px)
        }
    }
}
