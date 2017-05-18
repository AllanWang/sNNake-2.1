package ca.allanwang.snnake

import org.junit.Before
import org.junit.Test
import kotlin.test.*


/**
 * Created by Allan Wang on 2017-05-15.
 */
class MatrixTest {

    lateinit var matrix: Matrix

    @Before
    fun init() {
        matrix = Matrix(arrayOf(
                doubleArrayOf(1.0, 2.0, 3.0),
                doubleArrayOf(4.0, 5.0, 6.0)
        ))
    }

    @Test
    fun badMatrix() {
        try {
            Matrix(arrayOf(
                    doubleArrayOf(1.0, 2.0, 3.0),
                    doubleArrayOf(4.0, 5.0)
            ))
            fail("Did not catch bad matrix creation")
        } catch (e: MatrixException) {
            assertEquals("Matrix has varying row lengths", e.message, "Should throw varying row exception")
        }
    }

    @Test
    fun badMatrix2() {
        try {
            Matrix(2, 3, 1.0, 2.0, 3.0, 4.0, 5.0)
            fail("Did not catch bad matrix creation")
        } catch (e: MatrixException) {
            assertEquals("Matrix row col creation mismatch", e.message, "Should throw row col mismatch exception")
        }
    }

    @Test
    fun matrixConst() {
        val dup = Matrix(2, 3, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
        assertEquals(matrix, dup, "Matrix constructor by varargs should match main")
    }

    /**
     * Each number is printed with 12 characters by default
     * If number is not negative, a space will be prepended (so in this case you only see 11 characters including the decimal)
     */
    @Test
    fun string() {
        assertEquals("|  1.000000000  2.000000000  3.000000000 |\n|  4.000000000  5.000000000  6.000000000 |", matrix.toString().trim(), "Matrix should print row by row")
    }

    @Test
    fun equals() {
        val x = matrix.clone() - 1e-5
        assertFalse(matrix == x, "Matrices should no longer be equal")
        assertTrue(matrix.equals(x, 1e-4), "Matrices should be leniently equal")
    }

    @Test
    fun sumRows() {
        val result = Matrix(1, 3, 5.0, 7.0, 9.0)
        assertEquals(result, matrix.sumRows(), "sumRows should addSub all columns together and output a matrix with one row")
    }

    @Test
    fun badAdd() {
        val toAdd = Matrix(2, 2, 2.0)
        assertFalse(matrix.validate(Op.ADD, toAdd), "Cannot addSub 2 x 3 to 2 x 2")
        try {
            matrix + toAdd
            fail("Did not catch bad addition")
        } catch (e: MatrixException) {
            assertEquals(Op.ADD.errorMessage(matrix, toAdd), e.message)
        }
    }

    @Test
    fun add() {
        val toAdd = Matrix(2, 3, 1.0)
        val result = Matrix(2, 3,
                2.0, 3.0, 4.0,
                5.0, 6.0, 7.0)
        assertEquals(result, matrix + toAdd, "Matrix subtraction should be cell by cell")
    }

    //When we pass a matrix into a function, we should expect that matrix to remain unmodified from our operations
    @Test
    fun addInvulnerability() {
        assertNotEquals(matrix, addSelf(matrix.clone()), "Matrix addition in function should not affect original if cloned")
        assertEquals(matrix, addSelf(matrix), "Matrix addition in function should affect original if not cloned")
    }

    fun addSelf(m: Matrix): Matrix = m + m

    @Test
    fun negate() {
        val negation = Matrix(2, 3,
                -1.0, -2.0, -3.0,
                -4.0, -5.0, -6.0)
        assertEquals(negation, -matrix, "unaryMinus should be a scalar multiplication with -1")
    }

    @Test
    fun minus() {
        val subtrahend = Matrix(2, 3, 1.0)
        val result = Matrix(2, 3,
                0.0, 1.0, 2.0,
                3.0, 4.0, 5.0)
        assertEquals(result, matrix - subtrahend, "Matrix subtraction should be cell by cell")
    }

    @Test
    fun badTime() {
        val twoSized = Matrix(2, 2, 1.0)
        assertFalse(matrix.validate(Op.MULTIPLY, twoSized), "Cannot multiply 2 x 3 to 2 x 2")
        try {
            matrix * twoSized
            fail("Did not catch bad multiplication")
        } catch (e: MatrixException) {
            assertEquals(Op.MULTIPLY.errorMessage(matrix, twoSized), e.message)
        }
    }

    @Test
    fun times() {
        val threeSized = Matrix(3, 4,
                0.0, 1.0, 2.0, 3.0,
                3.0, 4.0, 5.0, -1.0,
                -3.0, -4.0, -5.0, -1.0)
        val result = Matrix(2, 4,
                -3.0, -3.0, -3.0, -2.0,
                -3.0, 0.0, 3.0, 1.0)
        assertEquals(3, matrix.cols, "Matrix col count should be 3")
        assertEquals(result, matrix * threeSized, "Matrix multiplication failed")
        assertEquals(2, matrix.rows, "Matrix row count should still be 2")
        assertEquals(4, matrix.cols, "Matrix col count should now be 4")
    }

    @Test
    fun timesConstant() {
        val result = Matrix(2, 3,
                2.0, 4.0, 6.0,
                8.0, 10.0, 12.0)
        assertEquals(result, matrix * 2.0, "Matrix scalar multiplication failed")
    }

    @Test
    fun transpose() {
        val orig = matrix.clone()
        val transpose = matrix.transpose()
        val result = Matrix(3, 2,
                1.0, 4.0,
                2.0, 5.0,
                3.0, 6.0)
        assertEquals(result, transpose, "Matrix transpose should mirror along y = -x")
        assertNotEquals(orig, transpose, "Matrix transpose should not affect cloned matrix")
    }

    @Test
    fun cloneEquality() {
        val clone = matrix.clone()
        assertEquals(matrix, clone, "Cloned matrix should be equal to original")
        matrix[0][0] = 2.0
        assertEquals(matrix, clone, "Cloned matrix should be still equal to original")
    }

    @Test
    fun deepClone() {
        val clone = matrix.deepClone()
        assertEquals(matrix, clone, "Deep cloned matrix should be equal to original")
        matrix[0][0] = 2.0
        assertEquals(1.0, clone[0][0], "Deep cloned still has val 1.0 at index 0 0")
        assertNotEquals(matrix, clone, "Deep cloned matrix should no longer be equal to original")
    }

    @Test
    fun row() {
        assertEquals(doubleArrayOf(1.0, 2.0, 3.0).contentToString(), matrix.row(0).contentToString(), "First row is 1, 2, 3")
    }

    @Test
    fun col() {
        assertEquals(doubleArrayOf(2.0, 5.0).contentToString(), matrix.col(1).contentToString(), "Second column is 2, 5")
    }

}