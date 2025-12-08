package card_transfer.card_transfer_backend;

import org.springframework.boot.SpringApplication;

public class TestCardTransferBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(CardTransferBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
