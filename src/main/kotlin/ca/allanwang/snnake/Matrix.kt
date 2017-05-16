package ca.allanwang.snnake

/**
 * Created by Allan Wang on 2017-05-15.
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
    val rows = matrix.size
    val cols = matrix[0].size

    constructor(rows: Int, cols: Int, vararg values: Double) : this(rows, cols) {
        if (values.size != rows * cols) throw MatrixException("Matrix row col creation mismatch")
        forEach { y, x, _ -> values[y * cols + x] }
    }

    constructor(rows: Int, cols: Int) : this(rows, cols, 0.0)
    constructor(rows: Int, cols: Int, value: Double) : this(Array(rows, { DoubleArray(cols, { _ -> value }) }))

    init {
        matrix.forEach {
            row ->
            if (row.size != cols)
                throw MatrixException("Matrix has varying row lengths")
        }
    }

    operator fun get(row: Int): DoubleArray = matrix[row]

    operator fun plus(m: Matrix): Matrix {
        Op.ADD.validateOrThrow(this, m)
        return forEach { y, x, value -> value + m[y][x] }
    }

    operator fun unaryMinus(): Matrix = forEach { i -> -i }

    operator fun minus(m: Matrix): Matrix {
        Op.SUBTRACT.validateOrThrow(this, m)
        return this + (-m)
    }

    operator fun times(m: Matrix): Matrix {
        Op.MULTIPLY.validateOrThrow(this, m)
        val orig = Matrix(matrix)
        matrix = Array(rows, { DoubleArray(m.cols) })
        forEach { y, x, _ -> multiply(y, x, orig, m) }
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun row(i: Int): DoubleArray = if (i < 0 || i > rows) doubleArrayOf() else matrix[i].clone()

    fun col(i: Int): DoubleArray = if (i < 0 || i > cols) doubleArrayOf() else DoubleArray(rows, { j -> matrix[j][i] })

    private fun forEach(mutation: (value: Double) -> Double): Matrix = forEach { _, _, value -> mutation(value) }

    private fun forEach(mutation: (y: Int, x: Int, value: Double) -> Double): Matrix {
        matrix.forEachIndexed {
            y, row ->
            row.forEachIndexed {
                x, v ->
                matrix[y][x] = mutation(y, x, v)
            }
        }
        return this
    }

    private fun mismatch(key: String, m: Matrix): Matrix {
        println(String.format("%s: size mismatch, %d x %d & %d x %d", key, rows, cols, m.rows, m.cols))
        return this
    }

    fun clone(): Matrix = Matrix(matrix)

    fun deepClone(): Matrix = Matrix(Array(rows) { matrix[it].clone() })

    override fun equals(other: Any?): Boolean = other is Matrix && matrix contentDeepEquals other.matrix

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