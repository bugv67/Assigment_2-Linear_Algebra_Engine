package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0]; // doesnt natter becuse we wont get here ?
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors shora
        if (matrix == null || matrix.length == 0) {
            this.vectors = new SharedVector[0];
            return;
        }
        this.vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            this.vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix
       
        SharedVector[] newMatrix = new SharedVector[matrix.length];
        SharedVector[] oldMatrix = this.vectors;
        acquireAllVectorWriteLocks(oldMatrix); // so that other wont work on un updated data
        try {

            for (int i = 0; i < matrix.length; i++) {
                newMatrix[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
            }
            this.vectors = newMatrix;
        } finally {
            releaseAllVectorWriteLocks(oldMatrix);
        }
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix

        SharedVector[] newMatrix = new SharedVector[matrix[0].length];
        SharedVector[] oldMatrix = this.vectors;
        acquireAllVectorWriteLocks(oldMatrix); // so that other wont work on un updated data
        try {
            for (int col = 0; col < matrix[0].length; col++) {
                double[] colArr = new double[matrix.length];
                for(int row = 0; row < matrix.length; row ++) {
                    colArr[row] = matrix[row][col];
      
                }
                newMatrix[col] = new SharedVector(colArr, VectorOrientation.COLUMN_MAJOR);
            }
            this.vectors = newMatrix;
        } finally {
            releaseAllVectorWriteLocks(oldMatrix);
        }
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        SharedVector[] oldMatrix = this.vectors;
        acquireAllVectorReadLocks(oldMatrix);
        try {

            if (oldMatrix == null || oldMatrix.length == 0) {
                throw new IllegalStateException("Matrix is empty");
            }
            VectorOrientation ori = oldMatrix[0].getOrientation();
            int col, row;
            double[][] result;

            // intialize result
            if (ori == VectorOrientation.ROW_MAJOR) {
                result = new double[oldMatrix.length][oldMatrix[0].length()];
                row = oldMatrix.length;
                col = oldMatrix[0].length();
            } else {
                result = new double[oldMatrix[0].length()][oldMatrix.length];
                row = oldMatrix[0].length();
                col = oldMatrix.length;
            }

            //fill the row 

            for (int i = 0; i < row; i++) { // each vector - does it matter row or column?
                for (int j = 0; j < col; j++) { // index in each vector
                    if (ori == VectorOrientation.ROW_MAJOR)
                        result[i][j] = oldMatrix[i].get(j);
                    else
                        result[i][j] = oldMatrix[j].get(i);

                }
            }
            return result;
        } finally

        {
            releaseAllVectorReadLocks(oldMatrix);
        }

    }

    public SharedVector get(int index) {
        // TODO: return vector at index

        if (index < 0 || index > vectors.length) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        vectors[index].readLock();
        try {
            return vectors[index];
        } finally {
            vectors[index].readUnlock();
        }
    }

    public int length() {
        // TODO: return number of stored vectors
        if (this.vectors == null) {
            return 0;
        }
        return this.vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        // what to do if null or empty??
        if (this.vectors == null || this.vectors.length == 0) {
            return VectorOrientation.ROW_MAJOR; // default to row major if empty
        }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for (int i = 0; i < vecs.length; i++) {
            vecs[i].readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks GHHHHH
        for (int i = 0; i < vecs.length; i++) {
            vecs[i].readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for (int i = 0; i < vecs.length; i++) {
            vecs[i].writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for (int i = 0; i < vecs.length; i++) {
            vecs[i].writeUnlock();
        }
    }
}
