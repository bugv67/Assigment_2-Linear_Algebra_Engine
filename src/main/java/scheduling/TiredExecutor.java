package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers; // all working and not working
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>(); // not working
    private final AtomicInteger inFlight = new AtomicInteger(0);
    private final Object completionLock = new Object();

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
            synchronized (completionLock) {
                inFlight.incrementAndGet();
            }

            Runnable taskWrapper = () -> {  // wraappint in order to follow the thread so that well be able to re insert her
            try {
                task.run();          // run the og task
            } finally {
                idleMinHeap.add(worker);  // return the worker to the heap becausr he is freeeeeee
                synchronized (completionLock) {
                    if (inFlight.decrementAndGet() == 0) {
                        completionLock.notifyAll(); //  alert finished
                    }
                } 
            }
        };

        worker.newTask(taskWrapper);

    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}


    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable task : tasks) {
            submit(task);
        }

        synchronized (completionLock) {
            while (inFlight.get() > 0) {
                try {
                    completionLock.wait(); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    public void shutdown() throws InterruptedException {
        if(inFlight.get() > 0 ) {
            throw new IllegalAccessError("Tried to shut down while there are still tasks to be completed");    
        }
        for(TiredThread worker : workers) {
                worker.shutdown();
        }
        for (TiredThread worker : workers) {
        worker.join(); //waiting for the threads to shut down
        }
        // TODO
    }

    public synchronized String getWorkerReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Worker Report ===\n");

        for (int i = 0; i < workers.length; i++) {
            report.append("Worker ")
                .append(workers[i].getWorkerId())
                .append(":\n");

            report.append("  Fatigue   : ")
                .append(workers[i].getFatigue())
                .append("\n");

            report.append("  Time Used : ")
                .append(workers[i].getTimeUsed())
                .append("\n");

            report.append("  Time Idle : ")
                .append(workers[i].getTimeIdle())
                .append("\n");

            report.append("\n");
    }

        return report.toString();
}

}
