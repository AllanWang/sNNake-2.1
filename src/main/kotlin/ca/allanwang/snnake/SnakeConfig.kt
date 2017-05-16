package ca.allanwang.snnake

/**
 * Created by Allan Wang on 2017-05-14.
 *
 * Holds the constants used in this game
 * Colors are available in [SnakeStyle.Companion]
 * Frame rate defaults are available above [SnakeView]
 */
const val gameHeight: Int = 40
const val gameWidth: Int = 80
const val blockSize = 15.0
const val snakeDefaultSize: Int = 3
val initOffset: Int = Math.min(gameHeight / 5, gameWidth / 5)
