package memory;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSharedVector {

    private SharedVector v1;
    private SharedVector v2;
    private SharedVector v5;

    @BeforeEach
    void setUp() {
        v1 = new SharedVector(new double[] { 1.0, 2.0, 3.0 }, VectorOrientation.ROW_MAJOR);
        v2 = new SharedVector(new double[] { 4.0, 5.0, 6.0 }, VectorOrientation.COLUMN_MAJOR);

        v5 = new SharedVector(new double[] { 6.0, 7.0, 8.0, 9.0, 11.0 }, VectorOrientation.ROW_MAJOR);
    }

    @Test
    void testConstructorAndGet() { // no use in v1 and v2 here because testing constructor
        double[] vector = { 1.0, 2.0, 3.0 };
        SharedVector v = new SharedVector(vector, VectorOrientation.ROW_MAJOR);

        assertEquals(3, v.length());
        assertEquals(1.0, v.get(0));
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());

        vector[0] = 99; // see if did deep copy
        assertEquals(1.0, v.get(0));
    }

    @Test
    void testGet() {
        assertEquals(1.0, v1.get(0), 1e-9);
        assertEquals(2.0, v1.get(1), 1e-9);
        assertEquals(3.0, v1.get(2), 1e-9);
    }

    @Test
    void testGetLength() {
        assertEquals(3, v1.length());
        assertEquals(5, v5.length());
    }

    @Test
    void testGetOrientation() {

        assertEquals(VectorOrientation.ROW_MAJOR, v1.getOrientation());
        assertEquals(VectorOrientation.COLUMN_MAJOR, v2.getOrientation());
    }

    @Test
    void testTranspose() {
        v2.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v2.getOrientation());

        v5.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v5.getOrientation());
    }

    @Test
    void testAdd() {
        SharedVector v3 = new SharedVector(new double[] { 1, 1, 1 }, VectorOrientation.ROW_MAJOR);
        v1.add(v3);

        assertEquals(2.0, v1.get(0), 1e-9);
        assertEquals(3.0, v1.get(1), 1e-9);
        assertEquals(4.0, v1.get(2), 1e-9);

    }

    @Test
    void testNegate() {
        v1.negate();
        assertEquals(-1.0, v1.get(0), 1e-9);
        assertEquals(-2.0, v1.get(1), 1e-9);
        assertEquals(-3.0, v1.get(2), 1e-9);

        v2.negate();
        assertEquals(-4.0, v2.get(0), 1e-9);
        assertEquals(-5.0, v2.get(1), 1e-9);
        assertEquals(-6.0, v2.get(2), 1e-9);
    }

    @Test
    void testDot() {
        double result = v1.dot(v2);
        assertEquals(32.0, result, 1e-9); // 1*4 + 2*5 + 3*6 = 32
    }

    @Test
    void testMul() {
        // Matrix:
        // [ 3 4 ]
        // [ 5 6 ]
        // [ 7 8 ]

        SharedMatrix matrix = new SharedMatrix(new double[][] { { 3, 4 },
                { 5, 6 },
                { 7, 8 } });

        // [1∗3 + 2∗5 + 3∗7 ]= 34
        // [1∗4 + 2∗6 + 3∗8 ]= 40
        double[] expected = { 34, 40 };
        v1.vecMatMul(matrix);

        assertEquals(2, v1.length());
        double[] result = { v1.get(0), v1.get(1) };

        assertArrayEquals(expected, result, 1e-9);
    }
}