package card_transfer.card_transfer_backend.unit.service;

import card_transfer.card_transfer_backend.dto.Amount;
import card_transfer.card_transfer_backend.model.Card;
import card_transfer.card_transfer_backend.repsitory.ConfirmOperationRepository;
import card_transfer.card_transfer_backend.repsitory.InMemoryCardsRepository;
import card_transfer.card_transfer_backend.service.CardTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardTransferServiceTest {

    @Mock
    private InMemoryCardsRepository cardsRepository;

    @Mock
    private ConfirmOperationRepository confirmOperationRepository;

    private CardTransferService cardTransferService;

    @BeforeEach
    void setUp() {
        cardTransferService = new CardTransferService(cardsRepository, confirmOperationRepository);
    }

    @Test
    void isCardMissing_CardExists_ReturnsFalse() {
        // Arrange
        String cardNumber = "1234567890123456";
        // Так как метод вызывает containsCard дважды, настраиваем мок на 2 вызова
        when(cardsRepository.containsCard(cardNumber)).thenReturn(true);

        // Act
        boolean result = cardTransferService.isCardMissing(cardNumber);

        // Assert - теперь ожидаем 2 вызова
        assertFalse(result);
        verify(cardsRepository, times(2)).containsCard(cardNumber);
    }

    @Test
    void isCardMissing_CardNotExists_ReturnsTrue() {
        // Arrange
        String cardNumber = "9999999999999999";
        // Первый вызов в if условии вернет false, поэтому выполнится return true
        when(cardsRepository.containsCard(cardNumber)).thenReturn(false);

        // Act
        boolean result = cardTransferService.isCardMissing(cardNumber);

        // Assert - только один вызов, так как метод вернет true после первого вызова
        assertTrue(result);
        verify(cardsRepository, times(1)).containsCard(cardNumber);
    }

    @Test
    void isCardMissing_NullCardNumber_ReturnsTrue() {
        // Act
        boolean result = cardTransferService.isCardMissing(null);

        // Assert - не должно быть вызовов к репозиторию
        assertTrue(result);
        verify(cardsRepository, never()).containsCard(anyString());
    }

    @Test
    void isCardMissing_BlankCardNumber_ReturnsTrue() {
        // Act
        boolean result = cardTransferService.isCardMissing("   ");

        // Assert - не должно быть вызовов к репозиторию
        assertTrue(result);
        verify(cardsRepository, never()).containsCard(anyString());
    }

    // Остальные тесты остаются без изменений...
    @Test
    void isBalanceSufficient_SufficientBalance_ReturnsTrue() {
        // Arrange
        Amount amount = new Amount(1000, "RUB");
        String cardNumber = "1234567890123456";
        String cardCVV = "123";
        String cardExpirationDate = "12/26";

        Card card = new Card(cardNumber, cardCVV, cardExpirationDate);
        when(cardsRepository.getCardBalance(card)).thenReturn(10000);

        // Act
        boolean result = cardTransferService.isBalanceSufficient(amount, cardNumber, cardCVV, cardExpirationDate);

        // Assert
        assertTrue(result);
        verify(cardsRepository, times(1)).getCardBalance(card);
    }

    @Test
    void isBalanceSufficient_InsufficientBalance_ReturnsFalse() {
        // Arrange
        Amount amount = new Amount(15000, "RUB");
        String cardNumber = "1234567890123456";
        String cardCVV = "123";
        String cardExpirationDate = "12/26";

        Card card = new Card(cardNumber, cardCVV, cardExpirationDate);
        when(cardsRepository.getCardBalance(card)).thenReturn(10000);

        // Act
        boolean result = cardTransferService.isBalanceSufficient(amount, cardNumber, cardCVV, cardExpirationDate);

        // Assert
        assertFalse(result);
    }

    @Test
    void isBalanceSufficient_NullBalance_ReturnsFalse() {
        // Arrange
        Amount amount = new Amount(1000, "RUB");
        String cardNumber = "1234567890123456";
        String cardCVV = "123";
        String cardExpirationDate = "12/26";

        Card card = new Card(cardNumber, cardCVV, cardExpirationDate);
        when(cardsRepository.getCardBalance(card)).thenReturn(null);

        // Act
        boolean result = cardTransferService.isBalanceSufficient(amount, cardNumber, cardCVV, cardExpirationDate);

        // Assert
        assertFalse(result);
    }

    @Test
    void transferMinus_CallsRepository() {
        // Arrange
        String cardNumber = "1234567890123456";
        Integer sum = 1000;

        // Act
        cardTransferService.transferMinus(cardNumber, sum);

        // Assert
        verify(cardsRepository, times(1)).setBalanceMinus(cardNumber, sum);
    }

    @Test
    void transferPlus_CallsRepository() {
        // Arrange
        String cardNumber = "1234567890123457";
        Integer sum = 1000;

        // Act
        cardTransferService.transferPlus(cardNumber, sum);

        // Assert
        verify(cardsRepository, times(1)).setBalancePlus(cardNumber, sum);
    }

    @Test
    void addIDAndCodeToRepo_CallsRepository() {
        // Arrange
        String id = "123456";
        String code = "9999";

        // Act
        cardTransferService.addIDAndCodeToRepo(id, code);

        // Assert
        verify(confirmOperationRepository, times(1)).setConfirmOperation(id, code);
    }

    @Test
    void getCodeTransfer_CallsRepository() {
        // Arrange
        String id = "123456";
        when(confirmOperationRepository.getConfirmOperation(id)).thenReturn("9999");

        // Act
        String result = cardTransferService.getCodeTransfer(id);

        // Assert
        assertEquals("9999", result);
        verify(confirmOperationRepository, times(1)).getConfirmOperation(id);
    }

    @Test
    void removeOperation_CallsRepository() {
        // Arrange
        String operationId = "123456";

        // Act
        cardTransferService.removeOperation(operationId);

        // Assert
        verify(confirmOperationRepository, times(1)).removeOperation(operationId);
    }
}