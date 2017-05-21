package ca.allanwang.snnake

import ca.allanwang.snnake.snake.SnakeVision

/**
 * Created by Allan Wang on 2017-05-14.
 *
 * Holds the constants used in this game
 * Colors are available in [ca.allanwang.snnake.snake.SnakeStyle.Companion]
 * Frame rate defaults are available above [ca.allanwang.snnake.snake.SnakeView]
 */
const val gameHeight: Int = 20
const val gameWidth: Int = 20
const val blockSize = 15.0
const val snakeDefaultSize: Int = 5
val snakeVision = SnakeVision._2
val initOffset: Int = Math.min(gameHeight / 5, gameWidth / 5)

