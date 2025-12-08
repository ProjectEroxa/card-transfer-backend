package card_transfer.card_transfer_backend.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransferID {

    public String generateID() {
        // Генерируем UUID, берем хеш, обрезаем до 6 цифр
        int hash = UUID.randomUUID().hashCode() & Integer.MAX_VALUE; // Положительное число
        return String.format("%06d", hash % 1_000_000);
    }
}
