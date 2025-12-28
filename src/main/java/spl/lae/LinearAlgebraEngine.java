package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

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
        this.executor = new TiredExecutor(numThreads);

    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        ComputationNode resolveable = computationRoot.findResolvable();
        while (resolveable != computationRoot) {

            loadAndCompute(resolveable);
            resolveable = computationRoot.findResolvable();
        }
        try {
            executor.shutdown();
        } catch (InterruptedException e) {
            // TODO: handle exception
            throw new IllegalAccessError("");
        }

        return resolveable;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor

        ComputationNodeType currOperator = node.getNodeType(); // not matrix bc resolvable
        // load the childern matrixes
        List<ComputationNode> children = node.getChildren();
        int size = node.getChildren().size();
        List<Runnable> tasks; // =new LinkedList<>(); no need
        if (size < 1) {
            throw new IllegalStateException("cannot comput this node");
        }
        if (size == 1) {
            ComputationNode child = children.getFirst();
            if (child.getNodeType() != ComputationNodeType.MATRIX) {
                throw new IllegalStateException("cannot copmute node whose child is not a matrix");
            }

            this.leftMatrix.loadRowMajor(child.getMatrix());
            if (currOperator == ComputationNodeType.TRANSPOSE) {
                tasks = createTransposeTasks();
            } else if (currOperator == ComputationNodeType.NEGATE) { //
                tasks = createNegateTasks();
            } else {
                throw new IllegalStateException(
                        "cannot compute oparation " + currOperator + " with more than 1 operand");
            }
        }
        for (int i = 0; i < size - 1; i++) {
            ComputationNode firstChild = children.removeFirst();
            ComputationNode secondChild = children.removeFirst();

            if (firstChild.getNodeType() != ComputationNodeType.MATRIX
                    || secondChild.getNodeType() != ComputationNodeType.MATRIX) {
                throw new IllegalStateException("cannot copmute node whose child is not a matrix");
            }

            this.leftMatrix.loadRowMajor(firstChild.getMatrix());
            this.rightMatrix.loadRowMajor(secondChild.getMatrix());

            if (currOperator == ComputationNodeType.ADD) {
                tasks = createAddTasks();
            } else { // currOperator==ComputationNodeType.MULTIPLY
                tasks = createMultiplyTasks();
            }

            this.executor.submitAll(tasks);
            ComputationNode result = new ComputationNode(this.leftMatrix.readRowMajor());
            children.addFirst(result);
        }
        node = new ComputationNode(this.leftMatrix.readRowMajor());

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
}
