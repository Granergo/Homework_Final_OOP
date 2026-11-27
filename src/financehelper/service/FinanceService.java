
package financehelper.service;

import financehelper.exception.FinanceException;
import financehelper.model.Category;
import financehelper.model.FinancialOperation;
import financehelper.model.OperationType;
import financehelper.model.Wallet;
import financehelper.repository.CategoryRepository;
import financehelper.repository.FinancialOperationRepository;
import financehelper.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class FinanceService {

    private final WalletRepository walletRepository;
    private final FinancialOperationRepository operationRepository;
    private final CategoryRepository categoryRepository;

    private long nextOperationId;

    public FinanceService(
            WalletRepository walletRepository,
            FinancialOperationRepository operationRepository,
            CategoryRepository categoryRepository
    ) {
        if (walletRepository == null
                || operationRepository == null
                || categoryRepository == null) {
            throw new IllegalArgumentException(
                    "Репозитории не должны быть пустыми."
            );
        }

        this.walletRepository = walletRepository;
        this.operationRepository = operationRepository;
        this.categoryRepository = categoryRepository;

        initializeDefaultCategories();

        long maxId = operationRepository.findAll()
                .stream()
                .mapToLong(FinancialOperation::getId)
                .max()
                .orElse(0);

        if (maxId == Long.MAX_VALUE) {
            throw new IllegalStateException(
                    "Достигнут предел идентификаторов операций."
            );
        }

        nextOperationId = maxId + 1;
    }

    // ==================== СОЗДАНИЕ ДОХОДА ====================

    public synchronized FinancialOperation addIncome(
            long walletId,
            BigDecimal amount,
            long categoryId
    ) {
        return addOperation(
                walletId,
                amount,
                categoryId,
                OperationType.INCOME
        );
    }

    // ==================== СОЗДАНИЕ РАСХОДА ====================

    public synchronized FinancialOperation addExpense(
            long walletId,
            BigDecimal amount,
            long categoryId
    ) {
        return addOperation(
                walletId,
                amount,
                categoryId,
                OperationType.EXPENSE
        );
    }

    private FinancialOperation addOperation(
            long walletId,
            BigDecimal amount,
            long categoryId,
            OperationType type
    ) {
        validateAmount(amount);

        Wallet wallet = findWallet(walletId);
        Category category = findCategory(categoryId);

        validateCategoryType(category, type);

        if (nextOperationId <= 0
                || nextOperationId == Long.MAX_VALUE) {
            throw new IllegalStateException(
                    "Достигнут предел идентификаторов операций."
            );
        }

        FinancialOperation operation = new FinancialOperation(
                nextOperationId,
                wallet.getId(),
                LocalDateTime.now(),
                amount,
                type,
                category.getId()
        );

        operationRepository.save(operation);
        nextOperationId++;

        return operation;
    }

    // ==================== РЕДАКТИРОВАНИЕ ОПЕРАЦИИ ====================

    public synchronized FinancialOperation updateOperation(
            long walletId,
            long operationId,
            BigDecimal newAmount,
            long newCategoryId
    ) {
        validateAmount(newAmount);

        FinancialOperation oldOperation =
                findWalletOperation(walletId, operationId);

        Category newCategory = findCategory(newCategoryId);

        // Тип операции после редактирования не меняется.
        validateCategoryType(
                newCategory,
                oldOperation.getType()
        );

        FinancialOperation updatedOperation =
                new FinancialOperation(
                        oldOperation.getId(),
                        oldOperation.getWalletId(),
                        oldOperation.getCreatedAt(),
                        newAmount,
                        oldOperation.getType(),
                        newCategory.getId()
                );

        operationRepository.update(updatedOperation);

        return updatedOperation;
    }

    // ==================== УДАЛЕНИЕ ОПЕРАЦИИ ====================

    public synchronized FinancialOperation deleteOperation(
            long walletId,
            long operationId
    ) {
        FinancialOperation operation =
                findWalletOperation(walletId, operationId);

        boolean deleted = operationRepository.deleteById(operationId);

        if (!deleted) {
            throw new IllegalStateException(
                    "Не удалось удалить финансовую операцию."
            );
        }

        return operation;
    }

    // ==================== ПОЛУЧЕНИЕ ОПЕРАЦИИ ====================

    public FinancialOperation getOperation(
            long walletId,
            long operationId
    ) {
        return findWalletOperation(walletId, operationId);
    }

    // ==================== БАЛАНС ====================

    public BigDecimal getBalance(long walletId) {
        return getTotalIncome(walletId)
                .subtract(getTotalExpenses(walletId));
    }

    public BigDecimal getTotalIncome(long walletId) {
        return getTotalByType(walletId, OperationType.INCOME);
    }

    public BigDecimal getTotalExpenses(long walletId) {
        return getTotalByType(walletId, OperationType.EXPENSE);
    }

    private BigDecimal getTotalByType(
            long walletId,
            OperationType type
    ) {
        validateWallet(walletId);

        return operationRepository.findByWalletId(walletId)
                .stream()
                .filter(operation -> operation.getType() == type)
                .map(FinancialOperation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== ПОЛНАЯ ИСТОРИЯ ====================

    public List<FinancialOperation> getOperations(long walletId) {
        validateWallet(walletId);

        return sortOperations(
                operationRepository.findByWalletId(walletId)
        );
    }

    // ==================== ПОИСК ПО КАТЕГОРИИ ====================

    public List<FinancialOperation> getOperationsByCategory(
            long walletId,
            long categoryId
    ) {
        validateWallet(walletId);
        findCategory(categoryId);

        List<FinancialOperation> operations =
                operationRepository.findByWalletId(walletId);

        return sortOperations(
                operations.stream()
                        .filter(operation ->
                                operation.getCategoryId() == categoryId
                        )
                        .toList()
        );
    }

    // ==================== ФИЛЬТРАЦИЯ ПО ПЕРИОДУ ====================

    public List<FinancialOperation> getOperationsBetween(
            long walletId,
            LocalDate from,
            LocalDate to
    ) {
        validateWallet(walletId);

        if (from == null || to == null) {
            throw new FinanceException(
                    "Необходимо указать начальную и конечную даты."
            );
        }

        if (from.isAfter(to)) {
            throw new FinanceException(
                    "Начальная дата не может быть позже конечной."
            );
        }

        List<FinancialOperation> operations =
                operationRepository.findByWalletId(walletId);

        return sortOperations(
                operations.stream()
                        .filter(operation -> {
                            LocalDate operationDate =
                                    operation.getCreatedAt().toLocalDate();

                            return !operationDate.isBefore(from)
                                    && !operationDate.isAfter(to);
                        })
                        .toList()
        );
    }

    // ==================== КАТЕГОРИИ ====================

    public List<Category> getCategories(OperationType type) {
        return categoryRepository.findByType(type);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public String getCategoryName(long categoryId) {
        return findCategory(categoryId).getName();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private List<FinancialOperation> sortOperations(
            List<FinancialOperation> operations
    ) {
        return operations.stream()
                .sorted(
                        Comparator.comparing(
                                FinancialOperation::getCreatedAt
                        ).reversed()
                )
                .toList();
    }

    private void validateWallet(long walletId) {
        findWallet(walletId);
    }

    private Wallet findWallet(long walletId) {
        if (walletId <= 0) {
            throw new FinanceException(
                    "Некорректный идентификатор кошелька."
            );
        }

        return walletRepository.findById(walletId)
                .orElseThrow(() -> new FinanceException(
                        "Кошелёк не найден."
                ));
    }

    private Category findCategory(long categoryId) {
        if (categoryId <= 0) {
            throw new FinanceException(
                    "Некорректный идентификатор категории."
            );
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new FinanceException(
                        "Указанная категория не найдена."
                ));
    }

    private FinancialOperation findWalletOperation(
            long walletId,
            long operationId
    ) {
        findWallet(walletId);

        if (operationId <= 0) {
            throw new FinanceException(
                    "Некорректный идентификатор операции."
            );
        }

        FinancialOperation operation =
                operationRepository.findById(operationId)
                        .orElseThrow(() -> new FinanceException(
                                "Операция не найдена."
                        ));

        if (operation.getWalletId() != walletId) {
            throw new FinanceException(
                    "Операция не принадлежит текущему кошельку."
            );
        }

        return operation;
    }

    private void validateCategoryType(
            Category category,
            OperationType expectedType
    ) {
        if (category.getType() != expectedType) {
            throw new FinanceException(
                    "Тип операции не соответствует выбранной категории."
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException(
                    "Сумма должна быть больше нуля."
            );
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw new FinanceException(
                    "Сумма не может содержать более двух знаков после запятой."
            );
        }
    }

    // ==================== НАЧАЛЬНЫЕ КАТЕГОРИИ ====================

    private void initializeDefaultCategories() {
        if (!categoryRepository.findAll().isEmpty()) {
            return;
        }

        categoryRepository.save(
                new Category(1, "Продукты", OperationType.EXPENSE)
        );

        categoryRepository.save(
                new Category(2, "Транспорт", OperationType.EXPENSE)
        );

        categoryRepository.save(
                new Category(3, "Развлечения", OperationType.EXPENSE)
        );

        categoryRepository.save(
                new Category(4, "Коммунальные услуги", OperationType.EXPENSE)
        );

        categoryRepository.save(
                new Category(5, "Зарплата", OperationType.INCOME)
        );

        categoryRepository.save(
                new Category(6, "Премия", OperationType.INCOME)
        );

        categoryRepository.save(
                new Category(7, "Прочие доходы", OperationType.INCOME)
        );

        categoryRepository.save(
                new Category(8, "Прочие расходы", OperationType.EXPENSE)
        );
    }
}
