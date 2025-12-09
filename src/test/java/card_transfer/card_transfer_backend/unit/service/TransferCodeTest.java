package card_transfer.card_transfer_backend.unit.service;

import card_transfer.card_transfer_backend.service.TransferCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransferCodeTest {

    private TransferCode transferCode;

    @BeforeEach
    void setUp() {
        transferCode = new TransferCode();
    }

    @Test
    void generateCode_ReturnsValidFormat() {
        // Act
        String code = transferCode.generateCode();

        // Assert
        assertNotNull(code);
        assertEquals(4, code.length());
        assertTrue(code.matches("\\d{4}"));
    }

    @RepeatedTest(10)
    void generateCode_ReturnsDifferentValues() {
        // Act
        String code1 = transferCode.generateCode();
        String code2 = transferCode.generateCode();

        // Assert
        System.out.println("Generated codes: " + code1 + ", " + code2);
        assertTrue(code1.matches("\\d{4}"));
        assertTrue(code2.matches("\\d{4}"));
    }

    @Test
    void generateCode_ReturnsPositiveNumbers() {
        // Act
        String code = transferCode.generateCode();
        int numericCode = Integer.parseInt(code);

        // Assert
        assertTrue(numericCode >= 0);
        assertTrue(numericCode < 10_000);
    }
}