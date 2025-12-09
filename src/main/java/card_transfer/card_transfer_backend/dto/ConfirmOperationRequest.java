package card_transfer.card_transfer_backend.dto;

import java.util.Objects;

public class ConfirmOperationRequest {
    private String operationId;
    private String confirmationCode;

    public ConfirmOperationRequest(String operationId, String confirmationCode) {
        this.operationId = operationId;
        this.confirmationCode = confirmationCode;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConfirmOperationRequest that = (ConfirmOperationRequest) o;
        return Objects.equals(operationId, that.operationId) && Objects.equals(confirmationCode, that.confirmationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, confirmationCode);
    }
}
