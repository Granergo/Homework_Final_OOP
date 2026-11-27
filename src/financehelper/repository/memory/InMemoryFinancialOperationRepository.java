
package financehelper.repository.memory;

import financehelper.model.FinancialOperation;
import financehelper.repository.FinancialOperationRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryFinancialOperationRepository
        implements FinancialOperationRepository {

    private final Map<Long, FinancialOperation> operations =
            new LinkedHashMap<>();

    @Override
    public void save(FinancialOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException(
                    "Финансовая операция не может быть пустой."
            );
        }

        if (operations.containsKey(operation.getId())) {
            throw new IllegalStateException(
                    "Операция с таким идентификатором уже существует."
            );
        }

        operations.put(operation.getId(), operation);
    }

    @Override
    public void update(FinancialOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException(
                    "Финансовая операция не может быть пустой."
            );
        }

        FinancialOperation existing = operations.get(operation.getId());

        if (existing == null) {
            throw new IllegalStateException(
                    "Операция для обновления не найдена."
            );
        }

        if (existing.getWalletId() != operation.getWalletId()) {
            throw new IllegalStateException(
                    "Нельзя перенести операцию в другой кошелёк."
            );
        }

        operations.put(operation.getId(), operation);
    }

    @Override
    public boolean deleteById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор операции должен быть положительным."
            );
        }

        return operations.remove(id) != null;
    }

    @Override
    public Optional<FinancialOperation> findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор операции должен быть положительным."
            );
        }

        return Optional.ofNullable(operations.get(id));
    }

    @Override
    public List<FinancialOperation> findByWalletId(long walletId) {
        if (walletId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор кошелька должен быть положительным."
            );
        }

        return operations.values()
                .stream()
                .filter(operation -> operation.getWalletId() == walletId)
                .toList();
    }

    @Override
    public List<FinancialOperation> findAll() {
        return List.copyOf(operations.values());
    }
}
