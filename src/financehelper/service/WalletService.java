
package financehelper.service;

import financehelper.exception.FinanceException;
import financehelper.model.Wallet;
import financehelper.repository.WalletRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public class WalletService {

    private final WalletRepository walletRepository;

    private long nextWalletId;

    public WalletService(WalletRepository walletRepository) {
        if (walletRepository == null) {
            throw new IllegalArgumentException(
                    "Репозиторий кошельков не может быть пустым."
            );
        }

        this.walletRepository = walletRepository;

        // Определяем следующий свободный ID.
        long maxWalletId = walletRepository.findAll()
                .stream()
                .mapToLong(Wallet::getId)
                .max()
                .orElse(0);

        if (maxWalletId == Long.MAX_VALUE) {
            throw new IllegalStateException(
                    "Невозможно создать новый идентификатор кошелька."
            );
        }

        nextWalletId = maxWalletId + 1;
    }

    // ==================== СОЗДАНИЕ КОШЕЛЬКА ====================

    public synchronized Wallet createWallet(long ownerUserId) {

        if (ownerUserId <= 0) {
            throw new FinanceException(
                    "Некорректный идентификатор пользователя."
            );
        }

        if (walletRepository.existsByOwnerUserId(ownerUserId)) {
            throw new FinanceException(
                    "У пользователя уже есть кошелёк."
            );
        }

        if (nextWalletId <= 0 || nextWalletId == Long.MAX_VALUE) {
            throw new IllegalStateException(
                    "Достигнут предел доступных идентификаторов кошельков."
            );
        }

        Wallet wallet = new Wallet(
                nextWalletId,
                ownerUserId,
                LocalDateTime.now()
        );

        walletRepository.save(wallet);

        nextWalletId++;

        return wallet;
    }

    // ==================== ПОЛУЧЕНИЕ КОШЕЛЬКА ====================

    public Optional<Wallet> findByOwnerUserId(long ownerUserId) {
        return walletRepository.findByOwnerUserId(ownerUserId);
    }

    // Получить существующий кошелёк или создать новый.
    public synchronized Wallet getOrCreateWallet(long ownerUserId) {
        return walletRepository.findByOwnerUserId(ownerUserId)
                .orElseGet(() -> createWallet(ownerUserId));
    }
}
