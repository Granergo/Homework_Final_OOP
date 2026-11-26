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

    // ==================== ДОХОДЫ ====================

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

    // ==================== РАСХОДЫ ====================

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

    // ==================== СОЗДАНИЕ ОПЕРАЦИИ ====================

    private FinancialOperation addOperation(
            long walletId,
            BigDecimal amount,
            long categoryId,
            OperationType type
    ) {
        validateAmount(amount);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new FinanceException(
                        "Кошелёк не найден."
                ));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new FinanceException(
                        "Указанная категория не найдена."
                ));

        if (category.getType() != type) {
            throw new FinanceException(
                    "Тип операции не соответствует выбранной категории."
            );
        }

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

    // ==================== БАЛАНС ====================

    public BigDecimal getBalance(long walletId) {
        BigDecimal income = getTotalIncome(walletId);
        BigDecimal expenses = getTotalExpenses(walletId);

        return income.subtract(expenses);
    }

    // ==================== ОБЩИЕ ДОХОДЫ ====================

    public BigDecimal getTotalIncome(long walletId) {
        return getTotalByType(walletId, OperationType.INCOME);
    }

    // ==================== ОБЩИЕ РАСХОДЫ ====================

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

    // ==================== ИСТОРИЯ ОПЕРАЦИЙ ====================

    public List<FinancialOperation> getOperations(long walletId) {
        validateWallet(walletId);

        return operationRepository.findByWalletId(walletId)
                .stream()
                .sorted(
                        Comparator.comparing(
                                FinancialOperation::getCreatedAt
                        ).reversed()
                )
                .toList();
    }

    // ==================== КАТЕГОРИИ ====================

    public List<Category> getCategories(OperationType type) {
        return categoryRepository.findByType(type);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public String getCategoryName(long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Категория #" + categoryId);
    }

    // ==================== ПРОВЕРКИ ====================

    private void validateWallet(long walletId) {
        if (walletId <= 0) {
            throw new FinanceException(
                    "Некорректный идентификатор кошелька."
            );
        }

        if (!walletRepository.findById(walletId).isPresent()) {
            throw new FinanceException(
                    "Кошелёк не найден."
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
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
