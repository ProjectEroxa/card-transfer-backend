package card_transfer.card_transfer_backend.repsitory;

import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ConfirmOperationRepository {
    // key - id, value - code
    private final ConcurrentHashMap<String, String> confirmOperationsStorage = new ConcurrentHashMap<>();

    //добавить в репозиторий
    public void setConfirmOperation(String id, String code) {
        confirmOperationsStorage.put(id, code);
    }

    //достать code по id операции из репозитория
    public String getConfirmOperation(String id) {
        return confirmOperationsStorage.get(id);
    }

    //удалить
    public void removeOperation(String operationId) {
        confirmOperationsStorage.remove(operationId);
    }
}
