package memory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSharedMatrix {

    // @BeforeEach
    // void setUp() {

    // }

    // TESTS FOR LENGTH() FUNC:
    
    @Test
    void caseVecsIsEmpty() {
        SharedMatrix matrix = new SharedMatrix();
        assertEquals(0,matrix.length());
    }

    // @Test
    // void caseVecsIsNotEmpty() {

    // }
    
}
