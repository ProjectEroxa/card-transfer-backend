package card_transfer.card_transfer_backend.repsitory;

import card_transfer.card_transfer_backend.model.Card;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCardsRepository {
    private final ConcurrentHashMap<Card, Integer> cardStorage = new ConcurrentHashMap<>(
            Map.of(
                    new Card("1234567890123456", "123", "12/26"), 10000,
                    new Card("1234567890123457", "321", "11/26"), 20000,
                    new Card("1234567890123458", "222", "11/27"), 30000)
    );

    // баланс по данным карты
    public Integer getCardBalance(Card card) {
        return cardStorage.get(card);
    }

    // наличие карты в хранилище
    public boolean containsCard(String cardNumber) {
        return cardStorage.keySet()
                .stream()
                .anyMatch(card -> card.getCardNumber().equals(cardNumber));
    }

    // меняют баланс карты

    public void setBalancePlus(String cardNumber, Integer sum) {
        for (Map.Entry<Card, Integer> entry : cardStorage.entrySet()) {
            if (entry.getKey().getCardNumber().equals(cardNumber)) {
                entry.setValue(entry.getValue() + sum);
            }
        }
    }

    public void setBalanceMinus(String cardNumber, Integer sum) {
        for (Map.Entry<Card, Integer> entry : cardStorage.entrySet()) {
            if (entry.getKey().getCardNumber().equals(cardNumber)) {
                entry.setValue(entry.getValue() - sum);
            }
        }
    }
}
