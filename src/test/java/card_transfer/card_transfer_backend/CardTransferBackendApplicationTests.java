package card_transfer.card_transfer_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CardTransferBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
