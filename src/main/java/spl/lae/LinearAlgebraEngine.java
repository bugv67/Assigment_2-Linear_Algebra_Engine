package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;
import scheduling.TiredExecutor;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Node;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        System.out.println("Build excuter!"); // SpecialPrint
        this.executor = new TiredExecutor(numThreads);

    }

    public ComputationNode run(ComputationNode computationRoot) {
        try {
            // TODO: resolve computation tree step by step until final matrix is produced
            System.out.println("In lae run"); // SpecialPrint
            ComputationNode resolveable = computationRoot.findResolvable();
            System.out.println("Found resolvable"); // SpecialPrint
            while (resolveable != null) {
                System.out.println("in while resolvable"); // SpecialPrint
                System.out.println("in while in the run"); // SpecialPrint

                List<ComputationNode> children = resolveable.getChildren();
                if (children.size() > 2) {
                    System.out.println("iside if>2"); // SpecialPrint
                    resolveable.associativeNesting();
                    System.out.println("resovable= " + resolveable); // SpecialPrint
                    resolveable = resolveable.findResolvable();
                    System.out.println("resovable= " + resolveable);
                }
                System.out.println("resovable= " + resolveable); // SpecialPrint
                try {
                    loadAndCompute(resolveable);
                } catch (Exception e) {
                    throw e;
                }

                resolveable = computationRoot.findResolvable();
            }
        } finally {
            try {
                executor.shutdown();
            } catch (InterruptedException e) {
                // TODO: handle exception
                System.out.println("executor shutdown interrupted");
            }
        }

        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        System.out.println("in load and compute"); // SpecialPrint

        ComputationNodeType currOperator = node.getNodeType(); // not matrix bc resolvable
        // load the childern matrixes
        List<ComputationNode> children = node.getChildren();
        int size = node.getChildren().size();
        List<Runnable> tasks = new LinkedList<>(); // no need
        if (size < 1) {
            throw new IllegalStateException("cannot comput this node");
        }
        System.out.println("size= " + size); // SpecialPrint
        if (size == 1) {
            ComputationNode child = children.getFirst();
            if (child.getNodeType() != ComputationNodeType.MATRIX) {
                throw new IllegalStateException("cannot copmute node whose child is not a matrix");
            }
            System.out.println("size==1"); // SpecialPrint
            try {
                validMatrix(child.getMatrix());
                validFunction(currOperator, child.getMatrix(), null);
                this.leftMatrix.loadRowMajor(child.getMatrix());
                if (currOperator == ComputationNodeType.TRANSPOSE) {
                    tasks = createTransposeTasks();
                } else if (currOperator == ComputationNodeType.NEGATE) { //
                    System.out.println("size= " + size); // SpecialPrint
                    tasks = createNegateTasks();
                } else {
                    throw new IllegalStateException(
                            "cannot compute oparation " + currOperator + " with more than 1 operand");
                }
            } catch (Exception e) {
                throw e;
            }

        }
        // for (int i = 0; i < size - 1; i++) {
        if (size > 1) {
            ComputationNode firstChild = children.getFirst();
            ComputationNode secondChild = children.get(1);
            // node.associativeNesting(); ---- in the run

            if (firstChild.getNodeType() != ComputationNodeType.MATRIX
                    || secondChild.getNodeType() != ComputationNodeType.MATRIX) {
                throw new IllegalStateException("cannot copmute node whose child is not a matrix");
            }

            validMatrix(firstChild.getMatrix());
            validMatrix(secondChild.getMatrix());
            validFunction(currOperator, firstChild.getMatrix(), secondChild.getMatrix());
            this.leftMatrix.loadRowMajor(firstChild.getMatrix());

            if (currOperator == ComputationNodeType.ADD) {
                this.rightMatrix.loadRowMajor(secondChild.getMatrix());
                tasks = createAddTasks();
            } else { // currOperator==ComputationNodeType.MULTIPLY
                this.rightMatrix.loadColumnMajor(secondChild.getMatrix());
                tasks = createMultiplyTasks();
            }

        }
        System.out.println("sunbitting tasks"); // SpecialPrint
        this.executor.submitAll(tasks);
        System.out.println("going to compute"); // SpecialPrint
        node.resolve(this.leftMatrix.readRowMajor());
        // }
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        LinkedList<Runnable> tasks = new LinkedList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIdx = i;
            Runnable task = () -> {
                leftMatrix.get(rowIdx).add(rightMatrix.get(rowIdx));
            };
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication

        LinkedList<Runnable> tasks = new LinkedList<>();
        for (int i = 0; i < this.leftMatrix.length(); i++) {
            final int rowIdx = i;
            Runnable task = () -> {
                SharedVector row = this.leftMatrix.get(rowIdx);
                row.vecMatMul(this.rightMatrix);
            };
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        System.out.println("create negate tasks"); // SpecialPrint
        LinkedList<Runnable> tasks = new LinkedList<>();
        for (int i = 0; i < this.leftMatrix.length(); i++) {
            final int rowIdx = i;
            Runnable task = () -> {
                this.leftMatrix.get(rowIdx).negate();
            };
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        LinkedList<Runnable> tasks = new LinkedList<>();

        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIdx = i;
            Runnable task = () -> {
                leftMatrix.get(rowIdx).transpose();
            };
            tasks.add(task);
        }

        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }

    private void validMatrix(double[][] matrix) throws IllegalArgumentException {
        // function to validate the matrix and the vectors before doing anything
        if (matrix == null || matrix.length == 0) {
            throw new IllegalArgumentException("matrix is null or empty");
        }
        int rowLength = matrix[0].length;
        for (int i = 1; i < matrix.length; i++) {
            double[] row = matrix[i];
            if (row == null || row.length == 0) {
                throw new IllegalArgumentException("matrix contains null or empty row");
            }
            if (row.length != rowLength) {
                throw new IllegalArgumentException("the matrix have different row lengths");
            }
        }

    }

    private void validFunction(ComputationNodeType operat, double[][] matrix1, double[][] matrix2)
            throws IllegalArgumentException {
        if (operat == ComputationNodeType.ADD) {
            if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
                throw new IllegalArgumentException("cannot add matrices with different dimensions");
            }
        }
        if (operat == ComputationNodeType.MULTIPLY) {
            if (matrix1[0].length != matrix2.length) {
                throw new IllegalArgumentException("cannot multiply matrices with incompatible dimensions");
            }
        }

    }
}
