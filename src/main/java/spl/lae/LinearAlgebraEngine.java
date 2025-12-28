package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        ComputationNode resolveable=computationRoot.findResolvable();
        while (resolveable!=computationRoot) {
            
            loadAndCompute(resolveable);
            resolveable=computationRoot.findResolvable();
        }
        return null;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor

      ComputationNodeType currOperator=  node.getNodeType(); //not matrix bc resolvable
       // load the childern matrixes
       List<ComputationNode> children= node.getChildren();
       int size= node.getChildren().size();
       List <Runnable> tasks;   //=new LinkedList<>(); no need
        if(size<1){
            throw new IllegalStateException("cannot comput this node");
        }
        if(size==1){
        ComputationNode child=children.getFirst();
        if(child.getNodeType()!= ComputationNodeType.MATRIX){
            throw new IllegalStateException("cannot copmute node whose child is not a matrix");
        }

        this.leftMatrix.loadRowMajor(child.getMatrix());
         if(currOperator==ComputationNodeType.TRANSPOSE){ 
            tasks=createTransposeTasks();
        }else if(currOperator==ComputationNodeType.NEGATE ) {  //
            tasks=createNegateTasks();
        } else{
            throw new IllegalStateException("cannot compute oparation "+ currOperator+" with more than 1 operand");
        }
      }
    for (int i = 0; i < size-1; i++) {
        ComputationNode firstChild=children.removeFirst();
        ComputationNode secondChild=children.removeFirst();

        if(firstChild.getNodeType()!= ComputationNodeType.MATRIX||secondChild.getNodeType()!= ComputationNodeType.MATRIX){
            throw new IllegalStateException("cannot copmute node whose child is not a matrix");
        }

        this.leftMatrix.loadRowMajor(firstChild.getMatrix());
        this.rightMatrix.loadRowMajor(secondChild.getMatrix());


        if(currOperator==ComputationNodeType.ADD){
            tasks=createAddTasks();
        }else{   // currOperator==ComputationNodeType.MULTIPLY
            tasks=createMultiplyTasks();
        }
        
        this.executor.submitAll(tasks);
        ComputationNode result= new ComputationNode(this.leftMatrix.readRowMajor());
        children.addFirst(result);
        }
        node= new ComputationNode(this.leftMatrix.readRowMajor());
        
}

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        return null;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        return null;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        return null;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        return null;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }
}
