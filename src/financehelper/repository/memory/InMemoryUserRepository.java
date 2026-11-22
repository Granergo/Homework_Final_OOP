
package financehelper.repository.memory;

import financehelper.model.User;
import financehelper.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {

    // Основное хранилище: ID пользователя -> объект User.
    private final Map<Long, User> usersById = new LinkedHashMap<>();

    // Дополнительный индекс: нормализованный логин -> ID пользователя.
    private final Map<String, Long> userIdsByLogin = new HashMap<>();

    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть пустым.");
        }

        long userId = user.getId();
        String normalizedLogin = normalizeLogin(user.getLogin());

        // Проверяем, не принадлежит ли ID другому логину.
        User existingUser = usersById.get(userId);

        if (existingUser != null) {
            String existingLogin = normalizeLogin(existingUser.getLogin());

            if (!existingLogin.equals(normalizedLogin)) {
                throw new IllegalStateException(
                        "Указанный идентификатор уже принадлежит другому пользователю."
                );
            }
        }

        // Проверяем уникальность логина.
        Long registeredUserId = userIdsByLogin.get(normalizedLogin);

        if (registeredUserId != null && !registeredUserId.equals(userId)) {
            throw new IllegalArgumentException(
                    "Пользователь с таким логином уже зарегистрирован."
            );
        }

        // Сохраняем пользователя в обоих хранилищах.
        usersById.put(userId, user);
        userIdsByLogin.put(normalizedLogin, userId);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        String normalizedLogin = normalizeLogin(login);

        Long userId = userIdsByLogin.get(normalizedLogin);

        if (userId == null) {
            return Optional.empty();
        }

        return findById(userId);
    }

    @Override
    public Optional<User> findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор пользователя должен быть положительным."
            );
        }

        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public boolean existsByLogin(String login) {
        String normalizedLogin = normalizeLogin(login);

        return userIdsByLogin.containsKey(normalizedLogin);
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(usersById.values());
    }

    // Приведение логина к единому формату для сравнения.
    private String normalizeLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым.");
        }

        return login.trim().toLowerCase(Locale.ROOT);
    }
}
