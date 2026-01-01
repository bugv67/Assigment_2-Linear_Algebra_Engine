package memory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SharedMatrixTest {


    // TEST CONSTRUCTORS
    @Test
    void nullMatrix_createsEmptyMatrix() {
        SharedMatrix m = new SharedMatrix((double[][]) null);
        assertEquals(0, m.length());
    }

    @Test
    void validMatrix_createsVaildMatrix() {
        double[][] data = {
                { 1, 2 },
                { 3, 4 }
        };

        SharedMatrix m = new SharedMatrix(data);
        assertEquals(2, m.length());
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
    }

    @Test
    void emptyConstructor_createsEmptyMatrix() {
        SharedMatrix m = new SharedMatrix();

        assertEquals(0, m.length());
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
        assertThrows(IllegalStateException.class, m::readRowMajor);
    }

    // TEST READROWMAJOR
    @Test
    void readRowMajor_rowMajorLoad_returnsSameMatrix() {
        double[][] data = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(data);

        double[][] result = m.readRowMajor();

        assertArrayEquals(data, result);
    }

    @Test
    void readRowMajor_columnMajorLoad_returnsSameMatrix() {
        double[][] data = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        double[][] result = m.readRowMajor();

        assertArrayEquals(data, result);
    }

    @Test
    void readRowMajor_onEmptyConstructor_throws() {
        SharedMatrix m = new SharedMatrix();

        assertThrows(IllegalStateException.class, m::readRowMajor);
    }

    @Test
    void readRowMajor_onEmptyMatrix_throws() {
        SharedMatrix m = new SharedMatrix(new double[0][0]);

        assertThrows(IllegalStateException.class, m::readRowMajor);
    }

    // TEST LOADCOLUMNMAJOR
    @Test
    void loadColumnMajor_emptyMatrix_throws() {
        SharedMatrix m = new SharedMatrix();
        assertThrows(Exception.class,
                () -> m.loadColumnMajor(new double[0][0]));
    }

    @Test
    void loadColumnMajor_OrientationAndLength() {
        double[][] data = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        assertEquals(3, m.length());
        assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
    }

    @Test
    void loadColumnMajor_Data() {
        double[][] data = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        // column 0 = [1, 3, 5]
        assertEquals(1, m.get(0).get(0));
        assertEquals(3, m.get(0).get(1));
        assertEquals(5, m.get(0).get(2));

        // column 1 = [2, 4, 6]
        assertEquals(2, m.get(1).get(0));
        assertEquals(4, m.get(1).get(1));
        assertEquals(6, m.get(1).get(2));
    }

    // TEST LOADROWMAJOR
    @Test
    void loadRowMajor_emptyMatrix_createsEmptyMatrix() {
        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(new double[0][0]);

        assertEquals(0, m.length());
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
    }

    @Test
    void loadRowMajor_orientationAndLength() {
        double[][] data = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(data);

        assertEquals(2, m.length());
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
    }

    @Test
    void loadRowMajor_storesEachRowCorrectly() {
        double[][] data = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(data);

        // row 0
        assertEquals(1, m.get(0).get(0));
        assertEquals(2, m.get(0).get(1));

        // row 1
        assertEquals(3, m.get(1).get(0));
        assertEquals(4, m.get(1).get(1));

        // row 2
        assertEquals(5, m.get(2).get(0));
        assertEquals(6, m.get(2).get(1));
    }

    // TEST GET
    @Test
    void get_validIndex_returnsCorrectVector() {
        double[][] data = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };
        SharedMatrix m = new SharedMatrix(data);

        SharedVector row0 = m.get(0);
        SharedVector row1 = m.get(1);

        assertEquals(VectorOrientation.ROW_MAJOR, row0.getOrientation());
        assertEquals(VectorOrientation.ROW_MAJOR, row1.getOrientation());

        assertEquals(1, row0.get(0));
        assertEquals(2, row0.get(1));
        assertEquals(3, row0.get(2));

        assertEquals(4, row1.get(0));
        assertEquals(5, row1.get(1));
        assertEquals(6, row1.get(2));
    }

    @Test
    void get_invalidIndex_throws() {
        double[][] data = {
                { 1, 2 },
                { 3, 4 }
        };
        SharedMatrix m = new SharedMatrix(data);

        assertThrows(IndexOutOfBoundsException.class, () -> m.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> m.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> m.get(100));
    }

    @Test
    void get_onEmptyMatrix_throws() {
        SharedMatrix m = new SharedMatrix();

        assertThrows(IndexOutOfBoundsException.class, () -> m.get(0));
    }

    // TEST LENGTH
    @Test
    void length_emptyConstructor_returnsZero() {
        SharedMatrix m = new SharedMatrix();
        assertEquals(0, m.length());
    }

    @Test
    void length_constructorWithMatrix_returnsRowCount() {
        double[][] data = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };
        SharedMatrix m = new SharedMatrix(data);

        assertEquals(3, m.length());
    }

    @Test
    void length_afterLoadRowMajor_updatesCorrectly() {
        double[][] data = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };
        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(data);
        assertEquals(2, m.length());
    }

    // TEST GETORIENTATION
    @Test
    void getOrientation_emptyConstructor_returnsRowMajor() {
        SharedMatrix m = new SharedMatrix();
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
    }

    @Test
    void getOrientation_rowMajorConstructor_returnsRowMajor() {
        double[][] data = {
                { 1, 2 },
                { 3, 4 }
        };
        SharedMatrix m = new SharedMatrix(data);

        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
    }

    @Test
    void getOrientation_afterLoadRowMajor_returnsRowMajor() {
        double[][] data = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };
        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(data);
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
    }

    @Test
    void getOrientation_afterLoadColumnMajor_returnsColumnMajor() {
        double[][] data = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);
        assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
    }

}