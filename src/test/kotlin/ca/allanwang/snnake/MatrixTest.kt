package ca.allanwang.snnake

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.fail


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

    @Test
    fun string() {
        assertEquals("| 1.0 2.0 3.0 |\n| 4.0 5.0 6.0 |", matrix.toString().trim(), "Matrix should print row by row")
    }

    @Test
    fun badAdd() {
        val toAdd = Matrix(2, 2, 2.0)
        assertFalse(matrix.validate(Op.ADD, toAdd), "Cannot add 2 x 3 to 2 x 2")
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
        assertEquals(result, matrix * threeSized, "Matrix multiplication failed")
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