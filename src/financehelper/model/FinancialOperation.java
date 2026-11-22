package financehelper.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FinancialOperation {

    private final long id;
    private final long walletId;
    private final LocalDateTime createdAt;
    private final BigDecimal amount;
    private final OperationType type;
    private final long categoryId;

    public FinancialOperation(
            long id,
            long walletId,
            LocalDateTime createdAt,
            BigDecimal amount,
            OperationType type,
            long categoryId
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("Номер операции должен быть положительным.");
        }

        if (walletId <= 0) {
            throw new IllegalArgumentException("Номер кошелька должен быть положительным.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Дата операции не указана.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма операции должна быть больше нуля.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Тип операции не указан.");
        }

        if (categoryId <= 0) {
            throw new IllegalArgumentException("Номер категории должен быть положительным.");
        }

        this.id = id;
        this.walletId = walletId;
        this.createdAt = createdAt;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
    }

    public long getId() {
        return id;
    }

    public long getWalletId() {
        return walletId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OperationType getType() {
        return type;
    }

    public long getCategoryId() {
        return categoryId;
    }
}