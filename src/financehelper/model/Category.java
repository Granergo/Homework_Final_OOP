package financehelper.model;

public class Category {

    private final long id;
    private final String name;
    private final OperationType type;

    public Category(long id, String name, OperationType type) {
        if (id <= 0) {
            throw new IllegalArgumentException("Номер категории должен быть положительным.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название категории не может быть пустым.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Тип категории не указан.");
        }

        this.id = id;
        this.name = name.trim();
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OperationType getType() {
        return type;
    }
}