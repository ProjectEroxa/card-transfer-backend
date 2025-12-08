package card_transfer.card_transfer_backend.controller;

import card_transfer.card_transfer_backend.dto.ConfirmOperationRequest;
import card_transfer.card_transfer_backend.service.TransferCode;
import card_transfer.card_transfer_backend.service.TransferID;
import card_transfer.card_transfer_backend.dto.TransferRequest;
import card_transfer.card_transfer_backend.service.CardTransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/transfer")
public class CardTransferController {
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
        cardTransferService.transferMinus(transferRequest.getCardFromNumber(), transferRequest.getAmount().getValue());
        cardTransferService.transferPlus(transferRequest.getCardToNumber(), transferRequest.getAmount().getValue());
        //генерим ID операции и добавляем в репо вместе с кодом
        String generatedID = transferID.generateID();
        String generatedCode = transferCode.generateCode();
        cardTransferService.addIDAndCodeToRepo(generatedID, generatedCode);
        //успешный ответ

        // ★ нужно для теста через постман ★
        System.out.println("=== TRANSFER CREATED ===");
        System.out.println("Generated operationId: " + generatedID);
        System.out.println("Generated code: " + generatedCode);
        System.out.println("Saved to repository");
        // ★ нужно для теста через постман ★

        return ResponseEntity.ok(Map.of("operationId", generatedID));
    }

    @PostMapping("/confirmOperation")
    public ResponseEntity<?> confirmOperation(@RequestBody ConfirmOperationRequest confirmOperationRequest) {

        // получаем ID от клиента
        String operationId = confirmOperationRequest.getOperationId();

        if (confirmOperationRequest.getOperationId() == null || confirmOperationRequest.getOperationId().isBlank()) {
            System.out.println("DEBUG: ID validation failed. ID is: " + confirmOperationRequest.getOperationId());
            throw new IllegalArgumentException("ID operation is absent");
        }

        if (confirmOperationRequest.getConfirmationCode() == null || confirmOperationRequest.getConfirmationCode().isBlank()) {
            System.out.println("DEBUG: Code validation failed. Code is: " + confirmOperationRequest.getConfirmationCode());
            throw new IllegalArgumentException("Code operation is absent");
        }

        if (!cardTransferService.getCodeTransfer(operationId).equals(confirmOperationRequest.getConfirmationCode())) {
            System.out.println("DEBUG: equals validation failed. Code is: " + confirmOperationRequest.getConfirmationCode());
            throw new IllegalArgumentException("Code operation is incorrect");
        }
        // удаляем использованную операцию (чтобы нельзя было подтвердить дважды)
        cardTransferService.removeOperation(operationId);
        //успешный ответ
        return ResponseEntity.ok(Map.of("operationId", operationId));
    }
}