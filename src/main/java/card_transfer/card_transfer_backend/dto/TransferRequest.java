package card_transfer.card_transfer_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class TransferRequest {
    @NotBlank
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private final String cardFromNumber;
    @NotBlank
    @Pattern(regexp = "\\d{2}/\\d{2}", message = "Valid till must be MM/YY")
    private final String cardFromValidTill;
    @NotBlank
    @Pattern(regexp = "\\d{3}", message = "CVV must be 3 digits")
    private final String cardFromCVV;
    @NotBlank
    @Pattern(regexp = "\\d{16}")
    private final String cardToNumber;
    @NotNull
    @Valid
    private final Amount amount;

    public TransferRequest(String cardFromNumber,
                           String cardFromValidTill,
                           String cardFromCVV,
                           String cardToNumber,
                           Amount amount) {
        this.cardFromNumber = cardFromNumber;
        this.cardFromValidTill = cardFromValidTill;
        this.cardFromCVV = cardFromCVV;
        this.cardToNumber = cardToNumber;
        this.amount = amount;
    }

    public String getCardFromValidTill() {
        return cardFromValidTill;
    }

    public String getCardFromCVV() {
        return cardFromCVV;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getCardFromNumber() {
        return cardFromNumber;
    }

    public String getCardToNumber() {
        return cardToNumber;
    }
}