package ca.allanwang.snnake

import javafx.beans.value.ChangeListener
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import tornadofx.*

/**
 * Created by Allan Wang on 2017-05-13.
 */
class SnakeApp : App(SnakeView::class, SnakeStyle::class) {
    init {
        reloadStylesheetsOnFocus()
    }
}

const val fpsDefault: Double = 20.0
const val fpsMin: Double = 0.0
const val fpsMax: Double = 60.0
const val fpsInc: Double = 2.0

class SnakeView : View("sNNake 2.0"), ViewContract {
    override fun draw(map: Array<Array<Int>>) {
        grid.children.filter { it is Rectangle }.forEach {
            rect ->
            MapData.color(rect as Rectangle, map[GridPane.getRowIndex(rect)][GridPane.getColumnIndex(rect)])
        }
    }

    override val root = VBox()

    val controller: SnakeGame by inject()
    private val player1 = ToggleGroup()
    private val player2 = ToggleGroup()
    lateinit var grid: GridPane
    private lateinit var fps: Text

    init {
        with(root) {
            borderpane {
                left = vbox {
                    hbox {
                        vbox {
                            label("Player 1")
                            radiobutton("Human", player1) {
                                isSelected = true
                            }
                            radiobutton("NN", player1)
                        }
                        vbox {
                            label("Player 2")
                            radiobutton("Human", player2)
                            radiobutton("NN", player2)
                            radiobutton("None", player2) {
                                isSelected = true
                            }

                        }
                        children.filter { it is VBox }.addClass(SnakeStyle.frame)
                    }
                    slider(fpsMin, fpsMax, fpsDefault, Orientation.HORIZONTAL) {
                        addClass(SnakeStyle.center)
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
                                fps.text = String.format("%d fps", newR.toLong())
                                controller.changeFps(newR)
                            }
                        }
                    }
                    fps = text(String.format("%d fps", controller.fps.toLong())){
                        addClass(SnakeStyle.center)
                    }
                    button("Start") {
                        addClass(SnakeStyle.center)
                        shortcut(KeyCode.SPACE.name)
                        action {
                            text = controller.playPause()
                        }
                    }
                }
                center = vbox {
                    grid = gridpane {
                        for (y in 0..gameHeight)
                            for (x in 0..gameWidth)
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
    }
}

interface ViewContract {
    fun draw(map: Array<Array<Int>>)
}

class SnakeStyle : Stylesheet() {
    companion object {
        val frame by cssclass()
        val center by cssclass()
        val apple = Color.RED
        val border = Color.WHITE
        val background = Color.BLACK
    }

    init {
        val padMedium = mixin {
            padding = box(20.px)
        }
        frame {
            +padMedium
        }
        center {
            +padMedium
            alignment = Pos.CENTER
        }
        radioButton {
            padding = box(5.px, 0.px)
        }
    }
}
