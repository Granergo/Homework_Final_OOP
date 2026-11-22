package financehelper.model;

import java.util.Arrays;

public class User {

    private final long id;
    private final String login;
    private final byte[] passwordHash;
    private final byte[] passwordSalt;

    public User(long id, String login, byte[] passwordHash, byte[] passwordSalt) {
        if (id <= 0) {
            throw new IllegalArgumentException("Номер пользователя должен быть положительным.");
        }

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым.");
        }

        if (passwordHash == null || passwordHash.length == 0) {
            throw new IllegalArgumentException("Хеш пароля не может быть пустым.");
        }

        if (passwordSalt == null || passwordSalt.length == 0) {
            throw new IllegalArgumentException("Добавка для пароля не может быть пустой.");
        }

        this.id = id;
        this.login = login;
        this.passwordHash = Arrays.copyOf(passwordHash, passwordHash.length);
        this.passwordSalt = Arrays.copyOf(passwordSalt, passwordSalt.length);
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public byte[] getPasswordHash() {
        return Arrays.copyOf(passwordHash, passwordHash.length);
    }

    public byte[] getPasswordSalt() {
        return Arrays.copyOf(passwordSalt, passwordSalt.length);
    }
}