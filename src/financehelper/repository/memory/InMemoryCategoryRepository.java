package financehelper.repository.memory;

import financehelper.model.Category;
import financehelper.model.OperationType;
import financehelper.repository.CategoryRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class InMemoryCategoryRepository implements CategoryRepository {

    private final Map<Long, Category> categories = new LinkedHashMap<>();

    @Override
    public void save(Category category) {
        if (category == null) {
            throw new IllegalArgumentException(
                    "Категория не может быть пустой."
            );
        }

        Category existing = categories.get(category.getId());

        if (existing != null) {
            throw new IllegalStateException(
                    "Категория с таким идентификатором уже существует."
            );
        }

        String normalizedName = normalize(category.getName());

        boolean duplicate = categories.values()
                .stream()
                .anyMatch(existingCategory ->
                        existingCategory.getType() == category.getType()
                                && normalize(existingCategory.getName())
                                .equals(normalizedName)
                );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Категория с таким названием уже существует."
            );
        }

        categories.put(category.getId(), category);
    }

    @Override
    public Optional<Category> findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор категории должен быть положительным."
            );
        }

        return Optional.ofNullable(categories.get(id));
    }

    @Override
    public List<Category> findByType(OperationType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Тип категории не указан."
            );
        }

        return categories.values()
                .stream()
                .filter(category -> category.getType() == type)
                .toList();
    }

    @Override
    public List<Category> findAll() {
        return List.copyOf(categories.values());
    }

    private String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
