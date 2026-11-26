package financehelper.repository;

import financehelper.model.Category;
import financehelper.model.OperationType;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    void save(Category category);

    Optional<Category> findById(long id);

    List<Category> findByType(OperationType type);

    List<Category> findAll();
}
