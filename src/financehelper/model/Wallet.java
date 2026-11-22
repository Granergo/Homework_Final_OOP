package financehelper.model;

import java.time.LocalDateTime;

public class Wallet {

    private final long id;
    private final long ownerUserId;
    private final LocalDateTime createdAt;

    public Wallet(long id, long ownerUserId, LocalDateTime createdAt) {
        if (id <= 0) {
            throw new IllegalArgumentException("Номер кошелька должен быть положительным.");
        }

        if (ownerUserId <= 0) {
            throw new IllegalArgumentException("Номер владельца должен быть положительным.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Дата создания кошелька не указана.");
        }

        this.id = id;
        this.ownerUserId = ownerUserId;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}