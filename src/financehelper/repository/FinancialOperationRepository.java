package financehelper.repository;

import financehelper.model.FinancialOperation;

import java.util.List;
import java.util.Optional;

public interface FinancialOperationRepository {

    // Сохранение новой операции.
    void save(FinancialOperation operation);

    // Обновление существующей операции.
    void update(FinancialOperation operation);

    // Удаление операции по ID.
    boolean deleteById(long id);

    // Поиск операции по ID.
    Optional<FinancialOperation> findById(long id);

    // Получение операций конкретного кошелька.
    List<FinancialOperation> findByWalletId(long walletId);

    // Получение всех операций.
    List<FinancialOperation> findAll();
}
