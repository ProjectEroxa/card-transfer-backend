package card_transfer.card_transfer_backend.service;

import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class TransferCode {

    public String generateCode() {
        // Генерируем UUID, берем хеш, обрезаем до 4 цифр
        int hash = UUID.randomUUID().hashCode() & Integer.MAX_VALUE; // Положительное число
        return String.format("%04d", hash % 10_000);
    }
}
