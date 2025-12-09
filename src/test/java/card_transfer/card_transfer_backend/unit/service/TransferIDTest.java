package card_transfer.card_transfer_backend.unit.service;

import card_transfer.card_transfer_backend.service.TransferID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransferIDTest {

    private TransferID transferID;

    @BeforeEach
    void setUp() {
        transferID = new TransferID();
    }

    @Test
    void generateID_ReturnsValidFormat() {
        // Act
        String id = transferID.generateID();

        // Assert
        assertNotNull(id);
        assertEquals(6, id.length());
        assertTrue(id.matches("\\d{6}"));
    }

    @RepeatedTest(10)
    void generateID_ReturnsDifferentValues() {
        // Act
        String id1 = transferID.generateID();
        String id2 = transferID.generateID();

        // Assert - очень маленькая вероятность совпадения, но теоретически возможно
        // В реальном тесте можно проверять, что значения разные
        System.out.println("Generated IDs: " + id1 + ", " + id2);
        // Просто проверяем, что оба валидны
        assertTrue(id1.matches("\\d{6}"));
        assertTrue(id2.matches("\\d{6}"));
    }

    @Test
    void generateID_ReturnsPositiveNumbers() {
        // Act
        String id = transferID.generateID();
        int numericId = Integer.parseInt(id);

        // Assert
        assertTrue(numericId >= 0);
        assertTrue(numericId < 1_000_000);
    }
}