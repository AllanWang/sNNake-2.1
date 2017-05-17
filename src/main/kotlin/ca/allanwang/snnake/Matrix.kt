package ca.allanwang.snnake

/**
 * Created by Allan Wang on 2017-05-15.
 *
 * [Matrix] holds a 2D double array, as well as executable operator functions for matrix manipulation
 * Matrices are not immutable, but their operations will only affect the matrix that is being operated on
 * Multiplication & transposition result in new matrix arrays, and
 * Addition & subtraction will first deepClone the matrix before the operation
 *
 * It is therefore advisable for functions to clone a matrix before operating on it so that it is not affected elsewhere
 * cloning is enough, as deepcloning is handled automatically when necessary
 */
class MatrixException(message: String) : RuntimeException(message)

enum class Op {
    ADD {
        override fun validate(m: Matrix, n: Matrix) = m.rows == n.rows && m.cols == n.cols
    },
    SUBTRACT {
        override fun validate(m: Matrix, n: Matrix) = m.rows == n.rows && m.cols == n.cols
    },
    MULTIPLY {
        override fun validate(m: Matrix, n: Matrix) = m.cols == n.rows
    };

    abstract fun validate(m: Matrix, n: Matrix): Boolean

    fun validateOrThrow(m: Matrix, n: Matrix) {
        if (!validate(m, n)) throw MatrixException(errorMessage(m, n))
    }

    fun errorMessage(m: Matrix, n: Matrix) = String.format("%s: size mismatch, (%d x %d) & (%d x %d)", toString(), m.rows, m.cols, n.rows, n.cols)
}

class Matrix(var matrix: Array<DoubleArray>) {
    val rows: Int
        get() = matrix.size
    val cols: Int
        get() = matrix[0].size

    constructor(rows: Int, cols: Int, vararg values: Double) : this(rows, cols) {
        if (values.size != rows * cols) throw MatrixException("Matrix row col creation mismatch")
        forEach { y, x, _ -> values[y * cols + x] }
    }

    constructor(rows: Int, cols: Int) : this(rows, cols, 0.0)
    constructor(rows: Int, cols: Int, value: Double) : this(Array(rows, { DoubleArray(cols, { value }) }))

    init {
        matrix.forEach {
            row ->
            if (row.size != cols)
                throw MatrixException("Matrix has varying row lengths")
        }
    }

    fun set(m: Matrix): Matrix {
        this.matrix = m.matrix
        return this
    }

    fun fill(value: Double): Matrix {
        return forEach { _ -> value }
    }

    operator fun get(row: Int): DoubleArray = matrix[row]
    operator fun get(row: Int, col: Int): Double = matrix[row][col]

    fun sumRows(): Matrix {
        matrix = Array(1, { DoubleArray(cols, { i -> col(i).sum() }) })
        return this
    }

    operator fun plus(m: Matrix): Matrix {
        Op.ADD.validateOrThrow(this, m)
        return forEach { y, x, value -> value + m[y][x] }
    }

    operator fun unaryMinus(): Matrix = forEach { i -> -i }

    operator fun minus(m: Matrix): Matrix {
        Op.SUBTRACT.validateOrThrow(this, m)
        return this + (-m)
    }

    /**
     * Multiplies each cell by [d]
     */
    operator fun times(d: Double): Matrix = forEach { value -> value * d }

    operator fun times(m: Matrix): Matrix {
        Op.MULTIPLY.validateOrThrow(this, m)
        val orig = clone()
        matrix = Array(rows, { DoubleArray(m.cols) })
        forEachNoClone { y, x, _ -> multiply(y, x, orig, m) }
        return this
    }

    private fun multiply(row: Int, col: Int, m: Matrix, n: Matrix): Double {
        var result = 0.0
        m[row].forEachIndexed {
            i, value ->
            result += value * n[i][col]
        }
        return result
    }

    fun validate(op: Op, m: Matrix): Boolean = op.validate(this, m)

    fun transpose(): Matrix {
        val orig = clone()
        matrix = Array(cols, { y -> kotlin.DoubleArray(rows, { x -> orig[x][y] }) })
        return this
    }

    fun row(i: Int): DoubleArray = if (i < 0 || i > rows) doubleArrayOf() else matrix[i].clone()

    fun col(i: Int): DoubleArray = if (i < 0 || i > cols) doubleArrayOf() else DoubleArray(rows, { j -> matrix[j][i] })

    fun forEach(mutation: (value: Double) -> Double): Matrix = forEach { _, _, value -> mutation(value) }

    fun forEach(mutation: (y: Int, x: Int, value: Double) -> Double): Matrix {
        matrix = deepClone().matrix
        return forEachNoClone(mutation)
    }

    private fun forEachNoClone(mutation: (y: Int, x: Int, value: Double) -> Double): Matrix {
        matrix.forEachIndexed {
            y, row ->
            row.forEachIndexed {
                x, v ->
                matrix[y][x] = mutation(y, x, v)
            }
        }
        return this
    }

    fun clone(): Matrix = Matrix(matrix)

    fun deepClone(): Matrix = Matrix(Array(rows) { matrix[it].clone() })

    override fun equals(other: Any?): Boolean = (other is Matrix && matrix contentDeepEquals other.matrix)

    override fun hashCode(): Int = matrix.contentDeepHashCode()

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("\n")
        matrix.forEach {
            row ->
            builder.append("| ")
            row.forEach {
                v ->
                builder.append(v).append(" ")
            }
            builder.append("|\n")
        }
        return builder.toString()
    }
}