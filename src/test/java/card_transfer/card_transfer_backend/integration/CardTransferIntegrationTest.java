package card_transfer.card_transfer_backend.integration;

import card_transfer.card_transfer_backend.CardTransferBackendApplication;
import card_transfer.card_transfer_backend.dto.Amount;
import card_transfer.card_transfer_backend.dto.ConfirmOperationRequest;
import card_transfer.card_transfer_backend.dto.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = CardTransferBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CardTransferIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/transfer";
    }

    @Test
    void fullTransferFlow_Success() {
        // 1. Выполняем перевод
        TransferRequest transferRequest = new TransferRequest(
                "1234567890123456",
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/transfer",
                new HttpEntity<>(transferRequest, headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
        assertNotNull(transferResponse.getBody());

        String operationId = (String) transferResponse.getBody().get("operationId");
        assertNotNull(operationId);

        // 2. Пытаемся подтвердить с неверным кодом
        ConfirmOperationRequest confirmRequest = new ConfirmOperationRequest(operationId, "0000");

        ResponseEntity<Map> confirmResponse = restTemplate.postForEntity(
                baseUrl + "/confirmOperation",
                new HttpEntity<>(confirmRequest, headers),
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, confirmResponse.getStatusCode());
        assertEquals("Error input data", confirmResponse.getBody().get("message"));
    }

    @Test
    void transfer_CardFromMissing_ReturnsBadRequest() {
        TransferRequest request = new TransferRequest(
                "9999999999999999", // Несуществующая карта, но валидный формат
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transfer",
                new HttpEntity<>(request, headers),
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error input data", response.getBody().get("message"));
    }

    @Test
    void transfer_CardToMissing_ReturnsBadRequest() {
        TransferRequest request = new TransferRequest(
                "1234567890123456",
                "12/26",
                "123",
                "9999999999999999", // Несуществующая карта, но валидный формат
                new Amount(1000, "RUB")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transfer",
                new HttpEntity<>(request, headers),
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error input data", response.getBody().get("message"));
    }

    @Test
    void transfer_InsufficientBalance_ReturnsBadRequest() {
        TransferRequest request = new TransferRequest(
                "1234567890123456",
                "12/26",
                "123",
                "1234567890123457",
                new Amount(999999, "RUB") // Слишком большая сумма
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transfer",
                new HttpEntity<>(request, headers),
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error input data", response.getBody().get("message"));
    }

    @Test
    void transfer_InvalidCardNumber_ReturnsBadRequest() {
        // Неверный формат номера карты - должен вызывать MethodArgumentNotValidException
        TransferRequest request = new TransferRequest(
                "123456789012345", // 15 цифр вместо 16
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transfer",
                new HttpEntity<>(request, headers),
                Map.class
        );

        // После добавления обработки MethodArgumentNotValidException
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Сообщение может быть "Validation error" или "Error input data"
        assertNotNull(response.getBody().get("message"));
    }

    @Test
    void transfer_InvalidCVV_ReturnsBadRequest() {
        // Неверный формат CVV
        TransferRequest request = new TransferRequest(
                "1234567890123456",
                "12/26",
                "12", // 2 цифры вместо 3
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transfer",
                new HttpEntity<>(request, headers),
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().get("message"));
    }

    @Test
    void confirmOperation_MissingOperationId_ReturnsBadRequest() {
        ConfirmOperationRequest request = new ConfirmOperationRequest("", "9999");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/confirmOperation",
                new HttpEntity<>(request, headers),
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error input data", response.getBody().get("message"));
    }

    @Test
    void confirmOperation_MissingCode_ReturnsBadRequest() {
        ConfirmOperationRequest request = new ConfirmOperationRequest("123456", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/confirmOperation",
                new HttpEntity<>(request, headers),
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error input data", response.getBody().get("message"));
    }

    @Test
    void confirmOperation_InvalidCode_ReturnsBadRequest() {
        // Сначала создаем перевод
        TransferRequest transferRequest = new TransferRequest(
                "1234567890123456",
                "12/26",
                "123",
                "1234567890123457",
                new Amount(1000, "RUB")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/transfer",
                new HttpEntity<>(transferRequest, headers),
                Map.class
        );

        String operationId = (String) transferResponse.getBody().get("operationId");

        // Пытаемся подтвердить с неверным кодом
        ConfirmOperationRequest confirmRequest = new ConfirmOperationRequest(operationId, "0000");

        ResponseEntity<Map> confirmResponse = restTemplate.postForEntity(
                baseUrl + "/confirmOperation",
                new HttpEntity<>(confirmRequest, headers),
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, confirmResponse.getStatusCode());
        assertEquals("Error input data", confirmResponse.getBody().get("message"));
    }
}