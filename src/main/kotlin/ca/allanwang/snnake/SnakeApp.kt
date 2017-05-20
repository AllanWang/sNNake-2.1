package ca.allanwang.snnake

import ca.allanwang.snnake.snake.SnakeStyle
import ca.allanwang.snnake.snake.SnakeView
import tornadofx.*

/**
 * Created by Allan Wang on 2017-05-19.
 */
class SnakeApp : App(SnakeView::class, SnakeStyle::class) {
    init {
        reloadStylesheetsOnFocus()
    }
}