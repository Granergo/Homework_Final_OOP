package financehelper.repository;

import financehelper.model.FinancialOperation;

import java.util.List;
import java.util.Optional;

public interface FinancialOperationRepository {

    void save(FinancialOperation operation);

    Optional<FinancialOperation> findById(long id);

    List<FinancialOperation> findByWalletId(long walletId);

    List<FinancialOperation> findAll();
}
