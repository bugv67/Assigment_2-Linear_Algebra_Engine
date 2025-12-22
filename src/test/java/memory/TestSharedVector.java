package memory;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSharedVector {

    @BeforeEach
    void setUp() {

    }

    @Test
    void testMul() {
        assertTrue(true);

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