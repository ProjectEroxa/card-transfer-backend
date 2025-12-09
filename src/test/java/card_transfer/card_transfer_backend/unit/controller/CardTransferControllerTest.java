package card_transfer.card_transfer_backend.unit.controller;

import card_transfer.card_transfer_backend.controller.CardTransferController;
import card_transfer.card_transfer_backend.controller.GlobalExceptionHandler;
import card_transfer.card_transfer_backend.dto.Amount;
import card_transfer.card_transfer_backend.dto.ConfirmOperationRequest;
import card_transfer.card_transfer_backend.dto.TransferRequest;
import card_transfer.card_transfer_backend.service.CardTransferService;
import card_transfer.card_transfer_backend.service.TransferCode;
import card_transfer.card_transfer_backend.service.TransferID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardTransferControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardTransferService cardTransferService;

    @Mock
    private TransferID transferID;

    @Mock
    private TransferCode transferCode;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        CardTransferController controller = new CardTransferController(
                cardTransferService, transferID, transferCode
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void transferCard_Success() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest(
                "1234567890123456",
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        when(cardTransferService.isCardMissing("1234567890123456")).thenReturn(false);
        when(cardTransferService.isCardMissing("1234567890123457")).thenReturn(false);
        when(cardTransferService.isBalanceSufficient(any(Amount.class), anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(transferID.generateID()).thenReturn("123456");
        when(transferCode.generateCode()).thenReturn("9999");

        // Act & Assert
        mockMvc.perform(post("/transfer/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value("123456"));

        verify(cardTransferService, times(1)).transferMinus("1234567890123456", 1000);
        verify(cardTransferService, times(1)).transferPlus("1234567890123457", 1000);
        verify(cardTransferService, times(1)).addIDAndCodeToRepo("123456", "9999");
    }

    @Test
    void transferCard_CardFromMissing_ThrowsException() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest(
                "1234567890123456", // Важно: валидный номер карты для прохождения валидации
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        when(cardTransferService.isCardMissing("1234567890123456")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/transfer/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error input data"));

        verify(cardTransferService, never()).transferMinus(anyString(), anyInt());
    }

    @Test
    void transferCard_CardToMissing_ThrowsException() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest(
                "1234567890123456",
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        when(cardTransferService.isCardMissing("1234567890123456")).thenReturn(false);
        when(cardTransferService.isCardMissing("1234567890123457")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/transfer/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error input data"));

        verify(cardTransferService, never()).isBalanceSufficient(any(), anyString(), anyString(), anyString());
    }

    @Test
    void transferCard_InsufficientBalance_ThrowsException() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest(
                "1234567890123456",
                "12/26",
                "123",
                "1234567890123457",
                new Amount(999999, "RUB")
        );

        when(cardTransferService.isCardMissing("1234567890123456")).thenReturn(false);
        when(cardTransferService.isCardMissing("1234567890123457")).thenReturn(false);
        when(cardTransferService.isBalanceSufficient(any(Amount.class), anyString(), anyString(), anyString()))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/transfer/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error input data"));

        verify(cardTransferService, never()).transferMinus(anyString(), anyInt());
    }

    @Test
    void transferCard_InvalidCardNumber_ValidationFailed() throws Exception {
        // Arrange - неверный формат номера карты (15 цифр вместо 16)
        TransferRequest request = new TransferRequest(
                "123456789012345", // 15 цифр
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        // Act & Assert - Spring Validation должен вернуть 400 через MethodArgumentNotValidException
        mockMvc.perform(post("/transfer/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    void confirmOperation_Success() throws Exception {
        // Arrange
        ConfirmOperationRequest request = new ConfirmOperationRequest("123456", "9999");

        when(cardTransferService.getCodeTransfer("123456")).thenReturn("9999");

        // Act & Assert
        mockMvc.perform(post("/transfer/confirmOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value("123456"));

        verify(cardTransferService, times(1)).removeOperation("123456");
    }

    @Test
    void confirmOperation_InvalidCode_ThrowsException() throws Exception {
        // Arrange
        ConfirmOperationRequest request = new ConfirmOperationRequest("123456", "0000");

        when(cardTransferService.getCodeTransfer("123456")).thenReturn("9999");

        // Act & Assert
        mockMvc.perform(post("/transfer/confirmOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error input data"));

        verify(cardTransferService, never()).removeOperation(anyString());
    }

    @Test
    void confirmOperation_MissingOperationId_ThrowsException() throws Exception {
        // Arrange
        ConfirmOperationRequest request = new ConfirmOperationRequest("", "9999");

        // Act & Assert
        mockMvc.perform(post("/transfer/confirmOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error input data"));
    }

    @Test
    void confirmOperation_MissingCode_ThrowsException() throws Exception {
        // Arrange
        ConfirmOperationRequest request = new ConfirmOperationRequest("123456", "");

        // Act & Assert
        mockMvc.perform(post("/transfer/confirmOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error input data"));
    }

    @Test
    void controller_InternalError_GlobalExceptionHandler() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest(
                "1234567890123456",
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        when(cardTransferService.isCardMissing("1234567890123456")).thenReturn(false);
        when(cardTransferService.isCardMissing("1234567890123457")).thenReturn(false);
        when(cardTransferService.isBalanceSufficient(any(Amount.class), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(post("/transfer/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error transfer"));
    }
}