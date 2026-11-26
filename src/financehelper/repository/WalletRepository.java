
package financehelper.repository;

import financehelper.model.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletRepository {

    // Сохранение нового кошелька.
    void save(Wallet wallet);

    // Поиск кошелька по идентификатору.
    Optional<Wallet> findById(long id);

    // Поиск кошелька по идентификатору владельца.
    Optional<Wallet> findByOwnerUserId(long ownerUserId);

    // Проверка наличия кошелька у пользователя.
    boolean existsByOwnerUserId(long ownerUserId);

    // Получение всех кошельков.
    List<Wallet> findAll();
}
