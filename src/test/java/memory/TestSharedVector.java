package memory;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSharedVector {

    @BeforeEach
    void setUp() {

    }

    @Test
    void testGet() {
        SharedVector vector = new SharedVector(new double[] { 10.5, 20.5, 30.5 }, VectorOrientation.ROW_MAJOR);
        assertEquals(10.5, vector.get(0), 1e-9);
        assertEquals(20.5, vector.get(1), 1e-9);
        assertEquals(30.5, vector.get(2), 1e-9);
    }

    @Test
    void testGetLength() {
        SharedVector vector = new SharedVector(new double[] { 1, 2, 3, 4, 5 }, VectorOrientation.ROW_MAJOR);
        assertEquals(5, vector.length());
    }

    @Test
    void testGetOrientation() {
        SharedVector vector = new SharedVector(new double[] { 1, 2, 3 }, VectorOrientation.COLUMN_MAJOR);
        assertEquals(VectorOrientation.COLUMN_MAJOR, vector.getOrientation());
    }

    @Test
    void testTranspose() {
        SharedVector vector = new SharedVector(new double[] { 1, 2, 3 }, VectorOrientation.ROW_MAJOR);
        vector.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, vector.getOrientation());

        vector.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, vector.getOrientation());
    }

    @Test
    void testAdd() {
        SharedVector vector1 = new SharedVector(new double[] { 1, 2, 3 }, VectorOrientation.ROW_MAJOR);
        SharedVector vector2 = new SharedVector(new double[] { 4, 5, 6 }, VectorOrientation.ROW_MAJOR);

        vector1.add(vector2);

        assertEquals(5.0, vector1.get(0), 1e-9);
        assertEquals(7.0, vector1.get(1), 1e-9);
        assertEquals(9.0, vector1.get(2), 1e-9);

    }

    @Test
    void testNegate() {
        SharedVector vector = new SharedVector(new double[] { 1, -2, 3 }, VectorOrientation.ROW_MAJOR);
        vector.negate();
        assertEquals(-1.0, vector.get(0), 1e-9);
        assertEquals(2.0, vector.get(1), 1e-9);
        assertEquals(-3.0, vector.get(2), 1e-9);
    }

    @Test
    void testDot() {
        SharedVector vector1 = new SharedVector(new double[] { 1, 2, 3 }, VectorOrientation.ROW_MAJOR);
        SharedVector vector2 = new SharedVector(new double[] { 4, 5, 6 }, VectorOrientation.COLUMN_MAJOR);
        double result = vector1.dot(vector2);
        assertEquals(32.0, result, 1e-9); // 1*4 + 2*5 + 3*6 = 32
    }

    @Test
    void testMul() {
        // assertTrue(true);
        // Row vector: [1, 2]
        SharedVector vector = new SharedVector(new double[] { 1, 2 }, VectorOrientation.ROW_MAJOR);

        // Matrix:
        // [ 3 4 ]
        // [ 5 6 ]

        SharedMatrix matrix = new SharedMatrix(
                new double[][] {
                        { 3, 4 },
                        { 5, 6 } });

        // Expected result:
        // [1*3 + 2*5, 1*4 + 2*6] = [13, 16]
        double[] expected = { 13, 16 };
        vector.vecMatMul(matrix);

        double[] result = { vector.get(0), vector.get(1) };

        assertArrayEquals(expected, result, 1e-9);
    }
}