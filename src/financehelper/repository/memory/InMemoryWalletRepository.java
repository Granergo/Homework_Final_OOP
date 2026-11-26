
package financehelper.repository.memory;

import financehelper.model.Wallet;
import financehelper.repository.WalletRepository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryWalletRepository implements WalletRepository {

    // Основное хранилище: ID кошелька -> объект Wallet.
    private final Map<Long, Wallet> walletsById = new LinkedHashMap<>();

    // Дополнительный индекс: ID владельца -> ID кошелька.
    private final Map<Long, Long> walletIdsByOwner = new HashMap<>();

    @Override
    public void save(Wallet wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException(
                    "Кошелёк не может быть пустым."
            );
        }

        long walletId = wallet.getId();
        long ownerUserId = wallet.getOwnerUserId();

        Wallet existingWallet = walletsById.get(walletId);

        // Один ID кошелька нельзя присвоить другому владельцу.
        if (existingWallet != null &&
                existingWallet.getOwnerUserId() != ownerUserId) {
            throw new IllegalStateException(
                    "Идентификатор кошелька уже принадлежит другому владельцу."
            );
        }

        // У одного пользователя не может быть несколько кошельков.
        Long registeredWalletId = walletIdsByOwner.get(ownerUserId);

        if (registeredWalletId != null &&
                registeredWalletId != walletId) {
            throw new IllegalStateException(
                    "У этого пользователя уже есть кошелёк."
            );
        }

        walletsById.put(walletId, wallet);
        walletIdsByOwner.put(ownerUserId, walletId);
    }

    @Override
    public Optional<Wallet> findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор кошелька должен быть положительным."
            );
        }

        return Optional.ofNullable(walletsById.get(id));
    }

    @Override
    public Optional<Wallet> findByOwnerUserId(long ownerUserId) {
        validateOwnerId(ownerUserId);

        Long walletId = walletIdsByOwner.get(ownerUserId);

        if (walletId == null) {
            return Optional.empty();
        }

        return findById(walletId);
    }

    @Override
    public boolean existsByOwnerUserId(long ownerUserId) {
        validateOwnerId(ownerUserId);

        return walletIdsByOwner.containsKey(ownerUserId);
    }

    @Override
    public List<Wallet> findAll() {
        return List.copyOf(walletsById.values());
    }

    private void validateOwnerId(long ownerUserId) {
        if (ownerUserId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор владельца должен быть положительным."
            );
        }
    }
}
