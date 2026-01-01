package scheduling;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestTiredExecutor {

    private TiredExecutor executor;
    private final int NUM_THREADS = 3;

    @BeforeEach
    void setUp() {
        executor = new TiredExecutor(NUM_THREADS);
    }

    @Test
    void testSubmit() {
        AtomicInteger counter = new AtomicInteger(0);
        executor.submit(() -> {
            counter.incrementAndGet();
        });

        try {
            Thread.sleep(100); // making sure thats done
        } catch (InterruptedException e) {

        }
        assertEquals(1, counter.get());
    }

    @Test
    void testSubmitAll() {
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(50); // working....
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executor.submitAll(tasks);
        assertEquals(5, counter.get());

        executor.submit(() -> { // making sure can handle more then size
            System.out.println("Running additional task");
        });
    }

    @Test
    void FailureGoToheap() {
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {
            throw new RuntimeException("simulate throw!");
        });

        assertDoesNotThrow(() -> executor.submitAll(tasks));

    }

    @Test
    void testShutdown() {
        Field field = null;
        try {
            field = TiredExecutor.class.getDeclaredField("inFlight");
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        field.setAccessible(true);
        AtomicInteger inFlightVal = null;
        try {
            inFlightVal = (AtomicInteger) field.get(executor);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        executor.submit(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        });
        int attempts = 0;
        while (inFlightVal.get() == 0 && attempts < 10) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            attempts++;
        }

        try {
            executor.shutdown();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        int currentInFlight = inFlightVal.get();
        System.out.println("inFlightVal = " + currentInFlight);

        assert (currentInFlight == 0);
    }
}
