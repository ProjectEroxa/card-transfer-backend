package card_transfer.card_transfer_backend.unit.repository;

import card_transfer.card_transfer_backend.model.Card;
import card_transfer.card_transfer_backend.repsitory.InMemoryCardsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCardsRepositoryTest {

    private InMemoryCardsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCardsRepository();
    }

    @Test
    void containsCard_CardExists_ReturnsTrue() {
        // Arrange
        String existingCardNumber = "1234567890123456";

        // Act
        boolean result = repository.containsCard(existingCardNumber);

        // Assert
        assertTrue(result);
    }

    @Test
    void containsCard_CardNotExists_ReturnsFalse() {
        // Arrange
        String nonExistingCardNumber = "9999999999999999";

        // Act
        boolean result = repository.containsCard(nonExistingCardNumber);

        // Assert
        assertFalse(result);
    }

    @Test
    void getCardBalance_CardExists_ReturnsBalance() {
        // Arrange
        Card card = new Card("1234567890123456", "123", "12/26");

        // Act
        Integer balance = repository.getCardBalance(card);

        // Assert
        assertEquals(10000, balance);
    }

    @Test
    void getCardBalance_CardNotExists_ReturnsNull() {
        // Arrange
        Card card = new Card("9999999999999999", "999", "12/99");

        // Act
        Integer balance = repository.getCardBalance(card);

        // Assert
        assertNull(balance);
    }

    @Test
    void setBalancePlus_IncreasesBalance() {
        // Arrange
        String cardNumber = "1234567890123456";
        int initialBalance = repository.getCardBalance(new Card(cardNumber, "123", "12/26"));

        // Act
        repository.setBalancePlus(cardNumber, 1000);

        // Assert
        Card card = new Card(cardNumber, "123", "12/26");
        Integer newBalance = repository.getCardBalance(card);
        assertEquals(initialBalance + 1000, newBalance);
    }

    @Test
    void setBalancePlus_NonExistingCard_NoEffect() {
        // Arrange
        String nonExistingCard = "9999999999999999";

        // Act
        repository.setBalancePlus(nonExistingCard, 1000);

        // Assert - не должно быть исключения
        // Баланс не меняется, так как карты нет
        assertFalse(repository.containsCard(nonExistingCard));
    }

    @Test
    void setBalanceMinus_DecreasesBalanceWithCommission() {
        // Arrange
        String cardNumber = "1234567890123456";
        Card card = new Card(cardNumber, "123", "12/26");
        int initialBalance = repository.getCardBalance(card);
        int amount = 1000;
        int commission = amount / 100; // 1%
        int expectedDeduction = amount + commission;

        // Act
        repository.setBalanceMinus(cardNumber, amount);

        // Assert
        Integer newBalance = repository.getCardBalance(card);
        assertEquals(initialBalance - expectedDeduction, newBalance);
    }

    @Test
    void setBalanceMinus_NonExistingCard_NoEffect() {
        // Arrange
        String nonExistingCard = "9999999999999999";

        // Act
        repository.setBalanceMinus(nonExistingCard, 1000);

        // Assert - не должно быть исключения
        assertFalse(repository.containsCard(nonExistingCard));
    }

    @Test
    void cardEquality_WorksCorrectly() {
        // Arrange
        Card card1 = new Card("1234567890123456", "123", "12/26");
        Card card2 = new Card("1234567890123456", "123", "12/26");
        Card card3 = new Card("1234567890123457", "123", "12/26");

        // Act & Assert
        assertEquals(card1, card2);
        assertNotEquals(card1, card3);
        assertEquals(card1.hashCode(), card2.hashCode());
        assertNotEquals(card1.hashCode(), card3.hashCode());
    }
}