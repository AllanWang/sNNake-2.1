package ca.allanwang.snnake

/**
 * Created by Allan Wang on 2017-05-18.
 */
class MatrixException(message: String) : RuntimeException(message)

enum class Op {
    ADD {
        override fun validate(m: Matrix, n: Matrix) = m.rows == n.rows && m.cols == n.cols
    },
    SUBTRACT {
        override fun validate(m: Matrix, n: Matrix) = ADD.validate(m, n)
    },
    MULTIPLY {
        override fun validate(m: Matrix, n: Matrix) = m.cols == n.rows
    },
    SCALAR_MULTIPLY {
        override fun validate(m: Matrix, n: Matrix) = ADD.validate(m, n)
    };

    abstract fun validate(m: Matrix, n: Matrix): Boolean

    fun validateOrThrow(m: Matrix, n: Matrix) {
        if (!validate(m, n)) throw MatrixException(errorMessage(m, n))
    }

    fun errorMessage(m: Matrix, n: Matrix) = "${toString()}: size mismatch, (${m.rows} x ${m.cols}) & (${n.rows} x ${n.cols})"
}

enum class Normalizer {
    //Divides each column by that column's max value
    COL_MAX {
        override fun apply(m: Matrix) {
            for (col in 0..m.cols - 1) {
                var max = m[0][col]
                (1..m.rows - 1).asSequence()
                        .filter { m[it][col] > max }
                        .forEach { max = m[it][col] }
                if (Math.abs(max) > 1.0)
                    (0..m.rows - 1).asSequence()
                            .forEach { m[it][col] /= max }
            }
        }
    };

    fun normalize(m: Matrix): Matrix {
        m.matrix = m.deepClone().matrix
        apply(m)
        return m
    }

    abstract internal fun apply(m: Matrix)
}