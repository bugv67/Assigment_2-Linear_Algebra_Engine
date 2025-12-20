package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
    // locking the object try { } catch() { } finally{ unlock }

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation

        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        this.vector = new double[vector.length];
        for (int i = 0; i < vector.length; i++) { // deep copy
            this.vector[i] = vector[i];
        }
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        // make sure number is in range??
        readLock();
        try {
            return this.vector[index];
        } finally {
            readUnlock();
        }
    }

    public int length() {
        // TODO: return vector length
        readLock();
        try {
            return this.vector.length;
        } finally {
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        readLock();
        try {
            return this.orientation;
        } finally {
            readUnlock();
        }
    }

    public void writeLock() {
        // TODO: acquire write lock
        this.lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        this.lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        this.lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        this.lock.readLock().unlock();
    }

    public void transpose() {
        // TODO: transpose vector

        writeLock();
        try {
            if (this.orientation == VectorOrientation.ROW_MAJOR) {
                this.orientation = VectorOrientation.COLUMN_MAJOR;
            } else {
                this.orientation = VectorOrientation.ROW_MAJOR;
            }
        } finally {
            writeUnlock();
        }

    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        // make sure operation is legal

        ////////////// to prevent dead lock if v1.add(v2) and v2.add(v1)

        // SharedVector first = this;
        // SharedVector second = other;

        // if (System.identityHashCode(first) > System.identityHashCode(second)) {
        // first = other;
        // second = this;
        // }
        // first.writeLock();
        // second.readLock();

        writeLock();
        other.readLock();
        try {
            if (other == null)
                return;
            if (this.orientation == other.orientation) {
                if (other.vector != null && other.length() == this.length()) {
                    for (int i = 0; i < length(); i++) {
                        this.vector[i] = this.vector[i] + other.vector[i];
                        // does vector orientation matter?? - mo
                    }
                }
            }
        } finally {
            writeUnlock();
            other.readUnlock();
            // second.readUnlock();
            // first.writeUnlock();
        }
    }

    public void negate() {
        // TODO: negate vector
        writeLock();
        try {
            for (int i = 0; i < length(); i++) {
                vector[i] = get(i) * -1;
            }
        } finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        // validation- what to return?

        ///////// dead lock????
        if (other == null)
            return 0; // ??

        readLock();
        other.readLock();
        try {
            if (other.length() != this.length()) {
                return 0;
            }
            if (this.getOrientation() != VectorOrientation.ROW_MAJOR
                    || other.getOrientation() != VectorOrientation.COLUMN_MAJOR)
                return 0; // ??

            double sum = 0;
            for (int i = 0; i < length(); i++) {
                sum = sum + (get(i) * other.get(i));
            }
            return sum;
        } finally {
            other.readUnlock();
            readUnlock();

        }

    }

    public void vecMatMul(SharedMatrix matrix) {

        double[][] matrix1 = matrix.readRowMajor(); // shora
        // TODO: compute row-vector × matrix
        // Validationn- what to return?
        if (matrix == null)
            return; // ??
        if (this.orientation != VectorOrientation.ROW_MAJOR)
            return; // ??

        double[] product = new double[matrix1[0].length];
        this.readLock();
        // matrix.acquireAllVectorReadLocks();

        try {
            // Matrix is organized as an array of rows
            for (int col = 0; col < matrix1[0].length; col++) {
                double sum = 0;
                for (int row = 0; row < this.length(); row++) {
                    // Vector[row] * Matrix[row][col]
                    sum += this.get(row) * matrix1[row][col];
                }
                product[col] = sum;
            }
        } finally {

            this.readUnlock();
        }

        writeLock();
        try {
            this.vector = product;
        } finally {
            writeUnlock();
        }
    }

}
