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
            TiredThread thread = new TiredThread(i, fatigueFactor);
            workers[i] = thread;
            idleMinHeap.add(thread);
            thread.start();
        }
    }

    public void submit(Runnable task) {
        System.out.println("in sumbit"); // SpecialPrint
        try {
            TiredThread worker = idleMinHeap.take(); //////// waits until worker free ??
            synchronized (completionLock) {
                inFlight.incrementAndGet();
            }

            Runnable taskWrapper = () -> { // wraappint in order to follow the thread so that well be able to re insert
                                           // her
                try {
                    System.out.println("running"); // SpecialPrint
                    task.run(); // run the og task
                } finally {
                    System.out.println("worker back to heap"); // SpecialPrint
                    idleMinHeap.add(worker); // return the worker to the heap becausr he is freeeeeee
                    synchronized (completionLock) { // if thread fails he relese this lock
                        if (inFlight.decrementAndGet() == 0) {
                            completionLock.notifyAll(); // alert finished
                        }
                    }
                }
            };

            worker.newTask(taskWrapper);

        } catch (InterruptedException e) {
            // return;
            Thread.currentThread().interrupt(); // ignore? will wake up in the sumbit of other thread
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        System.out.println("in submmitAll"); // SpecialPrint
        for (Runnable task : tasks) {
            submit(task);
        }

        synchronized (completionLock) {
            while (inFlight.get() > 0) {
                try {
                    completionLock.wait();
                } catch (InterruptedException e) { // ignore? will wake up in the sumbit of other thread
                    // return;
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        if (inFlight.get() > 0) {
            throw new IllegalAccessError("Tried to shut down while there are still tasks to be completed");
        }

        // Note: The 'inFlight' counter tracks only runnig tasks, not idle or waiting
        // Workers that are blocked on handoff.take() are not counted in 'inFlight'.
        // During shutdown, we wait until inFlight == 0, ensuring all submitted tasks
        // are completed,
        // then send a POISON_PILL to each worker to wake up any blocked threads and
        // close them safely.

        // synchronized (completionLock) {
        // while (inFlight.get() > 0) {
        // completionLock.wait();
        // }
        // }
        for (TiredThread worker : workers) {
            worker.shutdown();
        }
        for (TiredThread worker : workers) {
            worker.join(); // waiting for the threads to shut down
        }

    }

    public synchronized String getWorkerReport() {
        StringBuilder report = new StringBuilder();
        double faTigueAvg = 0.0;
        report.append("=== Worker Report ===\n");

        for (int i = 0; i < workers.length; i++) {
            faTigueAvg = faTigueAvg + workers[i].getFatigue();
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
        faTigueAvg = faTigueAvg / workers.length;
        double fairness = 0.0;
        for (int i = 0; i < workers.length; i++) {
            double deviation = workers[i].getFatigue() - faTigueAvg;
            fairness = fairness + Math.pow(deviation, 2);
        }
        report.append("Fairness: ").append(fairness).append("\n");

        return report.toString();
    }

}
