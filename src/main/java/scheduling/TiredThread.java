package scheduling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TiredThread extends Thread implements Comparable<TiredThread> {

    private static final Runnable POISON_PILL = () -> {
    }; // Special task to signal shutdown

    private final int id; // Worker index assigned by the executor
    private final double fatigueFactor; // Multiplier for fatigue calculation

    private final AtomicBoolean alive = new AtomicBoolean(true); // Indicates if the worker should keep running

    // Single-slot handoff queue; executor will put tasks here
    private final BlockingQueue<Runnable> handoff = new ArrayBlockingQueue<>(1);

    private final AtomicBoolean busy = new AtomicBoolean(false); // Indicates if the worker is currently executing a
                                                                 // task

    private final AtomicLong timeUsed = new AtomicLong(0); // Total time spent executing tasks
    private final AtomicLong timeIdle = new AtomicLong(0); // Total time spent idle
    private final AtomicLong idleStartTime = new AtomicLong(0); // Timestamp when the worker became idle

    public TiredThread(int id, double fatigueFactor) {
        this.id = id;
        this.fatigueFactor = fatigueFactor;
        this.idleStartTime.set(System.nanoTime());
        setName(String.format("FF=%.2f", fatigueFactor));
    }

    public int getWorkerId() {
        return id;
    }

    public double getFatigue() {
        return fatigueFactor * timeUsed.get();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public long getTimeUsed() {
        return timeUsed.get();
    }

    public long getTimeIdle() {
        return timeIdle.get();
    }

    /**
     * Assign a task to this worker.
     * This method is non-blocking: if the worker is not ready to accept a task,
     * it throws IllegalStateException.
     */
    public void newTask(Runnable task) {
        // TODO
        if (!alive.get()) {
            throw new IllegalStateException("Cannot assign a task to a dead thread: " + id);
        }
        if (isBusy()) {
            throw new IllegalStateException("This thread is busy: " + id);
        }
        boolean success =  this.handoff.offer(task);
        if (!success) {
            throw new IllegalStateException("This thread is not ready to accept a task: " + id);
        }
    }

    /**
     * Request this worker to stop after finishing current task.
     * Inserts a poison pill so the worker wakes up and exits.
     */
    public void shutdown() {
        // TODO
        long currTime = System.nanoTime();
        this.timeIdle.addAndGet(currTime - idleStartTime.get());
        this.alive.set(false);
        this.POISON_PILL.run();
    }

    @Override
    public void run() {
        // TODO
        while (true) {
            long currTime = System.nanoTime();
            try {
                Runnable task = this.handoff.take();
                if(task.equals(POISON_PILL)) {
                return;
            }
            this.timeIdle.addAndGet(currTime - idleStartTime.get());
            this.busy.set(true);
            task.run();
            } catch (Exception e) { 
           // TODOOOOOO  
            } finally {
                this.busy.set(false);
                long finishTime = System.nanoTime();
                this.timeUsed.addAndGet(finishTime - currTime);
                this.idleStartTime.set(finishTime);
            }
        }
       
    }

    @Override
    public int compareTo(TiredThread o) {
        // TODO
        if (o == null) {
            throw new IllegalArgumentException("thread cannot be null " + o.id);
        }
        // necessary??????
        if (!o.alive.get() || !this.alive.get()) {
            throw new IllegalArgumentException("threads cannot be dead " + id);
        }
        if(this.getFatigue() > o.getFatigue()) {
            return 1;
        } else if (this.getFatigue() < o.getFatigue()) {
            return -1;
        }
        return 0;
    }
}