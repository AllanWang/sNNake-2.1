package ca.allanwang.snnake.snake

import ca.allanwang.snnake.neuralnet.*
import javafx.scene.control.Alert
import java.awt.Desktop
import java.io.File
import java.io.IOException

/**
 * Created by Allan Wang on 2017-05-20.
 *
 * The core of the Neural Net. Contains all the arguments and the logic behind picking the next best direction
 */
enum class SnakeVision(vararg val layerSizes: Int, val activator: Activator = Activator.SIGMOID, val random: Random = Random.GAUSSIAN) {
    _1(6, 6, 3) {
        /**
         * We will feed 6 inputs to the neural network to decide the best direction to go to
         * All values are found relative to North, but are given in an order relative to the Snake's current direction
         * All values are also normalized (eg # of blocks -> # of blocks/total length)
         * Input 1      blocks to left of snake before nearest obstacle
         * Input 2      blocks in front of snake before nearest obstacle
         * Input 3      blocks to right of snake before nearest obstacle
         * Input x      blocks to back of snake before nearest obstacle (this will always be 0 except for the very first turn and will not be given to the NN
         * Input 4      horizontal block count to closest apple (right is positive)
         * Input 5      vertical block count to closest apple (up is positive)
         * Input 6      snake size/(game height * width)
         * Returns a 1 x 6 matrix
         */
        override fun getInputMatrix(map: Array<IntArray>, head: C, prevDirection: Directions, apples: List<C>, snakeSize: Int): Matrix {
            val (x, y) = head
            val mapWidth = map[0].size
            val mapHeight = map.size

            val obstacle = DoubleArray(4)
            val left = (x - 1 downTo 0).asSequence().indexOfFirst { MapData.get(map[y][it]).rating < 0 }
            obstacle[prevDirection.relativeIndex(0, 4)] = (if (left != -1) left else x).toDouble() / mapWidth

            val up = (y - 1 downTo 0).asSequence().indexOfFirst { MapData.get(map[it][x]).rating < 0 }
            obstacle[prevDirection.relativeIndex(1, 4)] = (if (up != -1) up else y).toDouble() / mapHeight

            val right = (x + 1..mapWidth - 1).asSequence().indexOfFirst { MapData.get(map[y][it]).rating < 0 }
            obstacle[prevDirection.relativeIndex(2, 4)] = (if (right != -1) right else mapWidth - 1 - x).toDouble() / mapWidth

            val down = (y + 1..mapHeight - 1).asSequence().indexOfFirst { MapData.get(map[it][x]).rating < 0 }
            obstacle[prevDirection.relativeIndex(3, 4)] = (if (down != -1) down else mapHeight - 1 - y).toDouble() / mapHeight

            val closestApple = head closest (apples.filter { a -> a != head }) ?: head
            val input = obstacle.slice(IntRange(0, 2)).toMutableList()
            val (ay, ax) = prevDirection.relativeAxis((y - closestApple.y).toDouble() / mapHeight, (closestApple.x - x).toDouble() / mapWidth)
            input.add(ax)
            input.add(ay)
            input.add(snakeSize.toDouble() / (mapHeight * mapWidth))
            return Matrix(1, 6, input)
        }

        /**
         *  Returns next direction based on output matrix, which is of the format [left, straight, right]
         *  The node with the biggest value will be the direction to go to
         */
        override fun getNextDirection(output: Matrix, prevDirection: Directions): Directions {
            val outputList = output.toList()
            return when (Math.max(Math.max(outputList[0], outputList[1]), outputList[2])) {
                outputList[0] -> prevDirection.left.value
                outputList[2] -> prevDirection.right.value
                else -> prevDirection
            }
        }
    },
    _2(5, 3, 1) {
        /**
         * We will feed 5 inputs to the neural network to assess the proposed direction
         * All values are found relative to North, but are given in an order relative to the Snake's current direction
         * Input 1      block rating at new position (obstacle: -1, empty: 0, apple: 1)
         * Input 2      block rating directly to the left (obstacle: -1, empty: 0, apple: 1)
         * Input 3      block rating directly in front (obstacle: -1, empty: 0, apple: 1)
         * Input 4      block rating directly to the right (obstacle: -1, empty: 0, apple: 1)
         * Input 5      apple delta (distance from closest apple before - distance from closest apple now)/distance from closest apple before
         * Since each input only assesses one direction, we will pass the values for each of [left, straight, right]
         * Returns a 3 x 5 matrix
         */
        override fun getInputMatrix(map: Array<IntArray>, head: C, prevDirection: Directions, apples: List<C>, snakeSize: Int): Matrix {
            val closestApple = head closest (apples.filter { a -> a != head }) ?: head
            val left = listOf(head.shift(-1, 0).getRating(map),
                    head.shift(-1, -1).getRating(map),
                    head.shift(-2, 0).getRating(map),
                    head.shift(-1, 1).getRating(map),
                    head.delta(closestApple, -1, 0))
            val up = listOf(head.shift(0, 1).getRating(map),
                    head.shift(-1, 1).getRating(map),
                    head.shift(0, 2).getRating(map),
                    head.shift(1, 1).getRating(map),
                    head.delta(closestApple, 0, 1))
            val right = listOf(head.shift(1, 0).getRating(map),
                    head.shift(1, 1).getRating(map),
                    head.shift(2, 0).getRating(map),
                    head.shift(1, -1).getRating(map),
                    head.delta(closestApple, 1, 0))
            val down = listOf(head.shift(0, -1).getRating(map),
                    head.shift(1, -1).getRating(map),
                    head.shift(0, -2).getRating(map),
                    head.shift(-1, -1).getRating(map),
                    head.delta(closestApple, 0, -1))
            val data = Array<List<Double>>(4, { listOf() })
            data[prevDirection.relativeIndex(0, 4)] = left
            data[prevDirection.relativeIndex(1, 4)] = up
            data[prevDirection.relativeIndex(2, 4)] = right
            data[prevDirection.relativeIndex(3, 4)] = down
            val input = data.slice(IntRange(0, 2)).toMutableList().flatMap { doubles -> doubles }
            return Matrix(3, 5, input)
        }

        /**
         *  Returns next direction based on output matrix, which is of the format [left, straight, right]
         *  The node with the biggest value will be the direction to go to
         */
        override fun getNextDirection(output: Matrix, prevDirection: Directions): Directions {
            val outputList = output.toList()
            return when (Math.max(Math.max(outputList[0], outputList[1]), outputList[2])) {
                outputList[0] -> prevDirection.left.value
                outputList[2] -> prevDirection.right.value
                else -> prevDirection
            }
        }
    };

    val key
        get() = "sNNake$name"

    val neuralNet
        get() = NeuralNet(layerSizes = *layerSizes, activator = activator, random = random)

    internal abstract fun getInputMatrix(map: Array<IntArray>, head: C, prevDirection: Directions, apples: List<C>, snakeSize: Int): Matrix
    internal abstract fun getNextDirection(output: Matrix, prevDirection: Directions): Directions
    fun getNextDirection(nn: NeuralNet, map: Array<IntArray>, head: C, prevDirection: Directions, apples: List<C>, snakeSize: Int): Directions = getNextDirection(nn.output(getInputMatrix(map, head, prevDirection, apples, snakeSize)), prevDirection)

    fun openDirectory() {
        val path = File(resourceBase, key)
        try {
            Desktop.getDesktop().open(path)
        } catch (e: IOException) {
            val alert = Alert(Alert.AlertType.ERROR)
            with(alert) {
                title = "Files not found"
                contentText = "Could not open file directory\n${path.absolutePath}"
            }
            alert.show()
        }
    }
}