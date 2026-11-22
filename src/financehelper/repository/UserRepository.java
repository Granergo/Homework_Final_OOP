
package financehelper.repository;

import financehelper.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    // Сохранение нового пользователя или обновление существующего.
    void save(User user);

    // Поиск пользователя по логину.
    Optional<User> findByLogin(String login);

    // Поиск пользователя по идентификатору.
    Optional<User> findById(long id);

    // Проверка существования пользователя с указанным логином.
    boolean existsByLogin(String login);

    // Получение списка всех пользователей.
    List<User> findAll();
}
