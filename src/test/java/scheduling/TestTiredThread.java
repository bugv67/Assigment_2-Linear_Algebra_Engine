package scheduling;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

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

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        System.out.println("task submitted to t1"); // SpecialPrint
        System.out.println("shutting down t1"); // SpecialPrint
        t1.shutdown();
        try {
            t1.join(2000); /////////////
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("t1 joined"); // SpecialPrint
        assertFalse(t1.isAlive());
    }

    @Test
    void compareTo() throws InterruptedException {
        System.out.println("Compare to test"); // SpecialPrint

        Method method = null;
        try {
            method = TiredThread.class.getDeclaredMethod("setTimeUsed", long.class);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        method.setAccessible(true);
        System.out.println("public the method"); // SpecialPrint
        long timeUsedT1 = 10000;

        try {
            method.invoke(t1, timeUsedT1);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("chage time to : " + timeUsedT1); // SpecialPrint
        assertEquals(10000, t1.getTimeUsed());
        assert (t2.compareTo(t1) < 0);

        System.out.println("test comareTo"); // SpecialPrint
        assertTrue(t2.compareTo(t1) < 0);
        assertTrue(t1.compareTo(t2) > 0);

    }
}
