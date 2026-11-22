package financehelper.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public class Budget {

    private final long id;
    private final long categoryId;
    private final BigDecimal amount;
    private final YearMonth period;

    public Budget(
            long id,
            long categoryId,
            BigDecimal amount,
            YearMonth period
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("Номер бюджета должен быть положительным.");
        }

        if (categoryId <= 0) {
            throw new IllegalArgumentException("Номер категории должен быть положительным.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Бюджет не может быть отрицательным.");
        }

        if (period == null) {
            throw new IllegalArgumentException("Период бюджета не указан.");
        }

        this.id = id;
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
    }

    public long getId() {
        return id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public YearMonth getPeriod() {
        return period;
    }
}