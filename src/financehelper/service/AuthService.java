
package financehelper.service;

import financehelper.exception.FinanceException;
import financehelper.model.User;
import financehelper.repository.UserRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService {

    // Параметры хеширования пароля.
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int HASH_ITERATIONS = 600_000;
    private static final int HASH_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    // Ограничения для регистрационных данных.
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    private static final Pattern LOGIN_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}._-]{3,32}");

    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    // Следующий идентификатор пользователя.
    private long nextUserId;

    // Текущий авторизованный пользователь.
    private User currentUser;

    // Данные для выполнения хеширования при неизвестном логине.
    private final byte[] dummySalt = new byte[SALT_LENGTH_BYTES];
    private final byte[] dummyHash = new byte[HASH_LENGTH_BITS / 8];

    public AuthService(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException(
                    "Репозиторий пользователей не может быть пустым."
            );
        }

        this.userRepository = userRepository;

        // Продолжаем нумерацию, учитывая уже загруженных пользователей.
        long maxUserId = userRepository.findAll()
                .stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0);

        if (maxUserId == Long.MAX_VALUE) {
            throw new IllegalStateException(
                    "Невозможно создать новый идентификатор пользователя."
            );
        }

        this.nextUserId = maxUserId + 1;

        // Создаём случайную соль для проверки неизвестных логинов.
        secureRandom.nextBytes(dummySalt);
    }

    // ==================== РЕГИСТРАЦИЯ ====================

    public synchronized User register(String login, char[] password) {
        try {
            if (currentUser != null) {
                throw new FinanceException(
                        "Сначала выйдите из текущего аккаунта."
                );
            }

            String validLogin = validateLogin(login);
            validatePassword(password);

            if (userRepository.existsByLogin(validLogin)) {
                throw new FinanceException(
                        "Пользователь с таким логином уже зарегистрирован."
                );
            }

            if (nextUserId <= 0) {
                throw new IllegalStateException(
                        "Достигнут предел доступных идентификаторов пользователей."
                );
            }

            // Генерируем уникальную соль для нового пользователя.
            byte[] passwordSalt = generateSalt();
            byte[] passwordHash = null;

            try {
                passwordHash = hashPassword(password, passwordSalt);

                User user = new User(
                        nextUserId++,
                        validLogin,
                        passwordHash,
                        passwordSalt
                );

                userRepository.save(user);

                return user;

            } finally {
                // Удаляем временные копии криптографических данных.
                Arrays.fill(passwordSalt, (byte) 0);

                if (passwordHash != null) {
                    Arrays.fill(passwordHash, (byte) 0);
                }
            }

        } finally {
            clearPassword(password);
        }
    }

    // ==================== АВТОРИЗАЦИЯ ====================

    public synchronized User login(String login, char[] password) {
        char[] passwordToCheck = password == null
                ? new char[0]
                : password;

        try {
            if (currentUser != null) {
                throw new FinanceException(
                        "Вы уже вошли в аккаунт. Сначала завершите текущий сеанс."
                );
            }

            String normalizedLogin = login == null
                    ? ""
                    : login.trim();

            Optional<User> userOptional = normalizedLogin.isEmpty()
                    ? Optional.empty()
                    : userRepository.findByLogin(normalizedLogin);

            // Если пользователь не найден, выполняем фиктивное хеширование.
            // Это уменьшает различия во времени ответа при неверном логине.
            if (userOptional.isEmpty()) {
                performDummyHash(passwordToCheck);

                throw new FinanceException(
                        "Неверный логин или пароль."
                );
            }

            User user = userOptional.get();

            byte[] passwordSalt = user.getPasswordSalt();
            byte[] storedHash = user.getPasswordHash();
            byte[] enteredHash = null;

            try {
                enteredHash = hashPassword(passwordToCheck, passwordSalt);

                // Сравниваем хеши с использованием постоянного по времени
                // сравнения для одинаковых по длине массивов.
                if (!MessageDigest.isEqual(storedHash, enteredHash)) {
                    throw new FinanceException(
                            "Неверный логин или пароль."
                    );
                }

                currentUser = user;

                return user;

            } finally {
                // Очищаем временные копии.
                Arrays.fill(passwordSalt, (byte) 0);
                Arrays.fill(storedHash, (byte) 0);

                if (enteredHash != null) {
                    Arrays.fill(enteredHash, (byte) 0);
                }
            }

        } finally {
            Arrays.fill(passwordToCheck, '\0');
        }
    }

    // ==================== ЗАВЕРШЕНИЕ СЕАНСА ====================

    public synchronized void logout() {
        currentUser = null;
    }

    // ==================== СОСТОЯНИЕ АВТОРИЗАЦИИ ====================

    public synchronized Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public synchronized boolean isAuthenticated() {
        return currentUser != null;
    }

    // ==================== ХЕШИРОВАНИЕ ПАРОЛЯ ====================

    private byte[] hashPassword(char[] password, byte[] salt) {
        PBEKeySpec keySpec = new PBEKeySpec(
                password,
                salt,
                HASH_ITERATIONS,
                HASH_LENGTH_BITS
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(HASH_ALGORITHM);

            return factory.generateSecret(keySpec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(
                    "Не удалось выполнить хеширование пароля.",
                    e
            );

        } finally {
            keySpec.clearPassword();
        }
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];

        secureRandom.nextBytes(salt);

        return salt;
    }

    private void performDummyHash(char[] password) {
        byte[] hash = hashPassword(password, dummySalt);

        try {
            MessageDigest.isEqual(hash, dummyHash);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    // ==================== ПРОВЕРКА ВВОДА ====================

    private String validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new FinanceException("Введите логин.");
        }

        String normalizedLogin = login.trim();

        if (!LOGIN_PATTERN.matcher(normalizedLogin).matches()) {
            throw new FinanceException(
                    "Логин должен содержать от 3 до 32 символов. " +
                            "Допустимы буквы, цифры, точка, дефис и подчёркивание."
            );
        }

        return normalizedLogin;
    }

    private void validatePassword(char[] password) {
        if (password == null) {
            throw new FinanceException("Введите пароль.");
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            throw new FinanceException(
                    "Пароль должен содержать не менее 8 символов."
            );
        }

        if (password.length > MAX_PASSWORD_LENGTH) {
            throw new FinanceException(
                    "Пароль не должен превышать 128 символов."
            );
        }
    }

    private void clearPassword(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
