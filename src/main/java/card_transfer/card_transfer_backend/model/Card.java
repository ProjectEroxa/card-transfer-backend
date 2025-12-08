package card_transfer.card_transfer_backend.model;

import java.util.Objects;

public class Card {
    private final String cardNumber;
    private final String cardCVV;
    private final String cardExpirationDate;

    public Card(String cardNumber, String cardCVV, String cardExpirationDate) {
        this.cardNumber = cardNumber;
        this.cardCVV = cardCVV;
        this.cardExpirationDate = cardExpirationDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardCVV() {
        return cardCVV;
    }

    public String getCardExpirationDate() {
        return cardExpirationDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, cardCVV, cardExpirationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(cardNumber, card.cardNumber)
                && Objects.equals(cardCVV, card.cardCVV)
                && Objects.equals(cardExpirationDate, card.cardExpirationDate);
    }
}
