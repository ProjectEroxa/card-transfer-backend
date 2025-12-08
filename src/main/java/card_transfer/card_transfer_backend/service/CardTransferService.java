package card_transfer.card_transfer_backend.service;

import card_transfer.card_transfer_backend.dto.Amount;
import card_transfer.card_transfer_backend.model.Card;
import card_transfer.card_transfer_backend.repsitory.ConfirmOperationRepository;
import card_transfer.card_transfer_backend.repsitory.InMemoryCardsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CardTransferService {

    private static final Logger log = LoggerFactory.getLogger(CardTransferService.class);
    InMemoryCardsRepository cardsRepository;
    ConfirmOperationRepository confirmOperationRepository;

    public CardTransferService(InMemoryCardsRepository cardsRepository,
                               ConfirmOperationRepository confirmOperationRepository) {
        this.cardsRepository = cardsRepository;
        this.confirmOperationRepository = confirmOperationRepository;
    }

    //проверяет наличие номера карты в бд
    public boolean isCardMissing(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank() || !cardsRepository.containsCard(cardNumber)) {
            log.info("cardNumber = {}  not found", cardNumber);
            return true;
        }
        log.info("cardNumber = {} accept in data", cardNumber);
        return !cardsRepository.containsCard(cardNumber);
    }

    //проверяет, хватит ли средств на счете
    public boolean isBalanceSufficient(Amount amount, String cardNumber, String cardCVV, String cardExpirationDate) {
        Integer balance = cardsRepository.getCardBalance(
                new Card(cardNumber, cardCVV, cardExpirationDate)
        );
        log.info("check balance = {}", balance);
        return balance != null && amount.getValue() <= balance;
    }

    //списывает средства с карты from
    public void transferMinus(String cardNumber, Integer sum) {
        log.info("transferMinus sum = {} ", sum);
        cardsRepository.setBalanceMinus(cardNumber, sum);
    }

    //зачисляет на карту to
    public void transferPlus(String cardNumber, Integer sum) {
        log.info("transferPlus sum = {} ", sum);
        cardsRepository.setBalancePlus(cardNumber, sum);
    }

    //добавляет в репозиторий ID и код операции
    public void addIDAndCodeToRepo(String id, String code) {
        log.info("addIDAndCodeToRepo id = {} code = {}", id, code);
        confirmOperationRepository.setConfirmOperation(id, code);
    }

    //достает из репозитория код по ID
    public String getCodeTransfer(String id) {
        log.info("getCodeTransfer id = {}", id);
        return confirmOperationRepository.getConfirmOperation(id);
    }

    // Удалить операцию после подтверждения
    public void removeOperation(String operationId) {
        log.info("removeOperation id = {}", operationId);
        confirmOperationRepository.removeOperation(operationId);
    }
}