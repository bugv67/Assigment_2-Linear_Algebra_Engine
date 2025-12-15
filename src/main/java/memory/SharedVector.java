package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        // make sure number is in range??
        return this.vector[index];
    }

    public int length() {
        // TODO: return vector length
        return this.vector.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        return this.orientation;
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
        if (this.orientation == VectorOrientation.ROW_MAJOR) {
            this.orientation = VectorOrientation.COLUMN_MAJOR;
        } else {
            this.orientation = VectorOrientation.ROW_MAJOR;
        }
    
    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        // make sure operation is legal
        if (other == null) return;
        if(other.vector != null && other.length() == this.length()) {
            for(int i = 0; i <length(); i++ ) {
                this.vector[i] = this.vector[i] + other.vector[i];
                //does vector orientation matter??
            }
        }
        return;
    }

    public void negate() {
        // TODO: negate vector
         for(int i = 0; i <length(); i++ ) {
                vector[i] = get(i) * -1;
            }
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        //validation??
        double sum = 0;
        for (int i = 0; i < length(); i++) {
            sum = sum + (get(i) * other.get(i));
        }
        return sum;
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        //Validation
        double[] product = new double[length()];
        for (int i = 0; i < length(); i++) {
            double sum = 0;
            for (int j = 0; j < matrix.length(); j++) {
            sum = sum + (get(i) * matrix.get(i).get(j));
             }
          product[i] = sum;
        }
    }
}
