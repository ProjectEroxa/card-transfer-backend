package card_transfer.card_transfer_backend.unit.repository;

import card_transfer.card_transfer_backend.repsitory.ConfirmOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfirmOperationRepositoryTest {

    private ConfirmOperationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ConfirmOperationRepository();
    }

    @Test
    void setConfirmOperation_StoresValue() {
        // Arrange
        String id = "123456";
        String code = "9999";

        // Act
        repository.setConfirmOperation(id, code);

        // Assert
        assertEquals(code, repository.getConfirmOperation(id));
    }

    @Test
    void getConfirmOperation_NotExists_ReturnsNull() {
        // Act
        String result = repository.getConfirmOperation("nonexistent");

        // Assert
        assertNull(result);
    }

    @Test
    void removeOperation_RemovesValue() {
        // Arrange
        String id = "123456";
        String code = "9999";
        repository.setConfirmOperation(id, code);

        // Act
        repository.removeOperation(id);

        // Assert
        assertNull(repository.getConfirmOperation(id));
    }

    @Test
    void removeOperation_NotExists_NoException() {
        // Act & Assert - не должно быть исключения
        repository.removeOperation("nonexistent");
    }

    @Test
    void concurrentOperations_ThreadSafe() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                String id = "id" + index;
                String code = "code" + index;
                repository.setConfirmOperation(id, code);
                assertEquals(code, repository.getConfirmOperation(id));
                repository.removeOperation(id);
                assertNull(repository.getConfirmOperation(id));
            });
            threads[i].start();
        }

        // Ждем завершения всех потоков
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - не должно быть исключений ConcurrentModificationException
        // Репозиторий должен быть потокобезопасным
    }
}