package scheduling;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

class TestTiredThread {

    private TiredThread t1;
    private TiredThread t2;

    @BeforeEach
    void setUp() {
        t1 = new TiredThread(0, 4.3);
        t2 = new TiredThread(1, 2.1);
    }

    @Test
    void getWorkerId() {
        System.out.println("test getWorkerId");
        assertEquals(0, ((TiredThread) t1).getWorkerId());
        assertEquals(1, ((TiredThread) t2).getWorkerId());
    }

    @Test
    void getFatigue() {
        System.out.println("test getFatigue");
        assertEquals(0, ((TiredThread) t1).getFatigue());
        assertEquals(0, ((TiredThread) t2).getFatigue());
    }

    @Test
    void isBusy() {
        System.out.println("test isBusy");
        assertFalse(((TiredThread) t1).isBusy());
        assertFalse(((TiredThread) t2).isBusy());
        t1.start();
        t1.newTask(() -> {
            int k = 0;
            assertTrue(((TiredThread) t1).isBusy());
            for (int i = 0; i < 5; i++) {
                k++;
            }
        });

        assertFalse(((TiredThread) t1).isBusy());
    }

    @Test
    void getTimeUsed() {
        System.out.println("test getTimeUsed");
        assertEquals(0, ((TiredThread) t1).getTimeUsed());
        assertEquals(0, ((TiredThread) t2).getTimeUsed());
        t1.start();
        t1.newTask(() -> {
            int k = 0;
            for (int i = 0; i < 30; i++) {
                k++;
            }
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertTrue(t1.getTimeUsed() > 0.000);
        System.out.println("Time used by t1: " + t1.getTimeUsed());
    }

    @Test
    void newTask() {
        System.out.println("test newTask");
        t1.start();
        assertDoesNotThrow(() -> {
            t1.newTask(() -> {
                int k = 0;
                for (int i = 0; i < 5; i++) {
                    k++;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {

                }
            });
        });

    }

    @Test
    void shutdown() {
        System.out.println("test shutdown");
        t1.start();
        System.out.println("started t1"); // SpecialPrint
        assertDoesNotThrow(() -> {
            t1.newTask(() -> {
                int k = 0;
                for (int i = 0; i < 5; i++) {
                    k++;
                }
            });
        });
        System.out.println("task submitted to t1"); // SpecialPrint
        System.out.println("shutting down t1"); // SpecialPrint
        t1.shutdown();
        try {
            t1.join();
        } catch (InterruptedException e) {

        }
        System.out.println("t1 joined"); // SpecialPrint
        assertFalse(t1.isAlive());
    }

    @Test
    void compareTo() throws InterruptedException {
        System.out.println("test comareTo"); // SpecialPrint
        assertTrue(t2.compareTo(t1) == 0);
        t1.start();
        // System.out.println("start in compaer to"); // SpecialPrint
        // t1.newTask(() -> {
        // try {
        // int k = 0;
        // for (int i = 0; i < 3; i++) {
        // k++;
        // }

        // } catch (Exception e) {
        // }
        // });
        // System.out.println("shutdown in compaer to"); // SpecialPrint
        // t1.shutdown();
        // try {
        // t1.join();
        // } catch (InterruptedException e) {
        //
        // }
        // System.out.println("T1 Fatigue: " + t1.getFatigue());
        // System.out.println("T2 Fatigue: " + t2.getFatigue());
        // System.out.println("Comparison Result: " + t2.compareTo(t1));

        // assertTrue(t2.compareTo(t1) > 0);
    }
    // t1.setTimeUsed(1000);
}
