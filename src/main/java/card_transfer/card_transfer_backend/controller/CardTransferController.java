package card_transfer.card_transfer_backend.controller;

import card_transfer.card_transfer_backend.dto.ConfirmOperationRequest;
import card_transfer.card_transfer_backend.dto.TransferRequest;
import card_transfer.card_transfer_backend.service.CardTransferService;
import card_transfer.card_transfer_backend.service.TransferCode;
import card_transfer.card_transfer_backend.service.TransferID;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/transfer")
public class CardTransferController {
    private static final Logger log = LoggerFactory.getLogger(CardTransferController.class);
    private final CardTransferService cardTransferService;
    private final TransferID transferID;
    private final TransferCode transferCode;

    public CardTransferController(CardTransferService cardTransferService,
                                  TransferID transferID,
                                  TransferCode transferCode) {
        this.cardTransferService = cardTransferService;
        this.transferID = transferID;
        this.transferCode = transferCode;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferCard(@Valid @RequestBody TransferRequest transferRequest) {

        // обработка исключений 500 срабатывает автоматически, так как есть GlobalExceptionHandler

        //в реквесте (request) содержится инфа о карте отправителя, получателе и сумме перевода

        //проверка карты отправителя || получателя
        if (cardTransferService.isCardMissing(transferRequest.getCardFromNumber()) ||
                cardTransferService.isCardMissing(transferRequest.getCardToNumber())) {
            throw new IllegalArgumentException("Check the cards details");
        }

        //проверка на достаточность баланса карты
        if (!cardTransferService.isBalanceSufficient(transferRequest.getAmount(),
                transferRequest.getCardFromNumber(),
                transferRequest.getCardFromCVV(),
                transferRequest.getCardFromValidTill())) {
            throw new IllegalArgumentException("Check the balance - not enough funds");
        }

        // выполняем перевод и зачисление после всех проверок, отправляем ответ с json в теле ответа (id)
        log.info("Transfer request: {}", transferRequest);
        cardTransferService.transferMinus(transferRequest.getCardFromNumber(), transferRequest.getAmount().getValue());
        cardTransferService.transferPlus(transferRequest.getCardToNumber(), transferRequest.getAmount().getValue());
        //генерим ID операции и добавляем в репо вместе с кодом
        String generatedID = transferID.generateID();
        log.info("Generated operationId: {}", generatedID);
        String generatedCode = transferCode.generateCode();
        log.info("Generated code: {}", generatedCode);
        cardTransferService.addIDAndCodeToRepo(generatedID, generatedCode);
        //успешный ответ

        // ★ нужно для теста через постман ★
        System.out.println("=== TRANSFER CREATED ===");
        System.out.println("Generated operationId: " + generatedID);
        System.out.println("Generated code: " + generatedCode);
        // ★ нужно для теста через постман ★
        log.info("=== TRANSFER CREATED ===");
        return ResponseEntity.ok(Map.of("operationId", generatedID));
    }

    @PostMapping("/confirmOperation")
    public ResponseEntity<?> confirmOperation(@RequestBody ConfirmOperationRequest confirmOperationRequest) {

        // получаем ID от клиента
        String operationId = confirmOperationRequest.getOperationId();

        if (confirmOperationRequest.getOperationId() == null || confirmOperationRequest.getOperationId().isBlank()) {
            log.info("ID validation failed. ID is: {}", confirmOperationRequest.getOperationId());
            throw new IllegalArgumentException("ID operation is absent");
        }

        if (confirmOperationRequest.getConfirmationCode() == null || confirmOperationRequest.getConfirmationCode().isBlank()) {
            log.info("Code validation failed. Code is: {}", confirmOperationRequest.getConfirmationCode());
            throw new IllegalArgumentException("Code operation is absent");
        }

        if (!cardTransferService.getCodeTransfer(operationId).equals(confirmOperationRequest.getConfirmationCode())) {
            log.info("Equals validation failed. Code is: {}", confirmOperationRequest.getConfirmationCode());
            throw new IllegalArgumentException("Code operation is incorrect");
        }
        // удаляем использованную операцию (чтобы нельзя было подтвердить дважды)
        log.info("Operation with ID {} was removed from repository", operationId);
        cardTransferService.removeOperation(operationId);
        //успешный ответ
        log.info("Operation with ID {} was confirmed", operationId);
        return ResponseEntity.ok(Map.of("operationId", operationId));
    }
}