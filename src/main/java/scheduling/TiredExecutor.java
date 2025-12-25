package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers; // all working and not working
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>(); // not working
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        workers = new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = Math.random() + 0.5;
            TiredThread thread = new TiredThread(i,fatigueFactor);
            workers[i] = thread;
            idleMinHeap.add(thread);
            thread.start();
        }
    }
    public void submit(Runnable task) {
        try {
            TiredThread worker = idleMinHeap.take(); //////// waits until worker free ?? 
           inFlight.incrementAndGet();

            Runnable taskWrapper = () -> {  // wraappint in order to follow the thread so that well be able to re insert her
            try {
                task.run();          // run the og task
            } finally {
                idleMinHeap.add(worker);  // return the worker to the heap becausr he is freeeeeee
                inFlight.decrementAndGet(); 
            }
        };

        worker.newTask(taskWrapper);

    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}


    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        for(Runnable task : tasks) {
            submit(task);
        }
        

    }

    public void shutdown() throws InterruptedException {
        // TODO
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        return null;
    }
}
