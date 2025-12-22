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
        acquireAllVectorWriteLocks(vectors); // so that other wont work on un updated data
        SharedVector[] newMarix = new SharedVector[matrix.length];
        SharedVector[] oldMarix = this.vectors;
        try {

            for (int i = 0; i < matrix.length; i++) {
                newMarix[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
            }
            this.vectors = newMarix;
        } finally {
            releaseAllVectorWriteLocks(oldMarix);
        }
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        acquireAllVectorWriteLocks(vectors); // so that other wont work on un updated data
        // do we need to flip the data? becuse each row is a vector
        SharedVector[] newMarix = new SharedVector[matrix.length];
        SharedVector[] oldMarix = this.vectors;
        try {
            for (int i = 0; i < matrix.length; i++) {
                newMarix[i] = new SharedVector(matrix[i], VectorOrientation.COLUMN_MAJOR);
            }
            this.vectors = newMarix;
        } finally {
            releaseAllVectorWriteLocks(oldMarix);
        }
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        SharedVector[] snapshot = this.vectors;
        acquireAllVectorReadLocks(snapshot);
        try {

            if (snapshot == null || snapshot.length == 0) {
                throw new IllegalStateException("Matrix is empty");
            }
            VectorOrientation ori = snapshot[0].getOrientation();
            int col, row;
            double[][] result;

            if (ori == VectorOrientation.ROW_MAJOR) {
                result = new double[snapshot.length][snapshot[0].length()];
                row = snapshot.length;
                col = snapshot[0].length();
            } else {
                result = new double[snapshot[0].length()][snapshot.length];
                row = snapshot[0].length();
                col = snapshot.length;
            }

            for (int i = 0; i < row; i++) { // each vector - does it matter row or column?
                for (int j = 0; j < col; j++) { // index in each vector
                    if (ori == VectorOrientation.ROW_MAJOR)
                        result[i][j] = snapshot[i].get(j);
                    else
                        result[i][j] = snapshot[j].get(i);

                }
            }
            return result;
        } finally

        {
            releaseAllVectorReadLocks(snapshot);
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
