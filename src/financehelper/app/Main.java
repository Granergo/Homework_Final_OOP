
package financehelper.app;

import financehelper.exception.FinanceException;
import financehelper.model.Category;
import financehelper.model.FinancialOperation;
import financehelper.model.OperationType;
import financehelper.model.User;
import financehelper.model.Wallet;
import financehelper.repository.CategoryRepository;
import financehelper.repository.FinancialOperationRepository;
import financehelper.repository.UserRepository;
import financehelper.repository.WalletRepository;
import financehelper.repository.memory.InMemoryCategoryRepository;
import financehelper.repository.memory.InMemoryFinancialOperationRepository;
import financehelper.repository.memory.InMemoryUserRepository;
import financehelper.repository.memory.InMemoryWalletRepository;
import financehelper.service.AuthService;
import financehelper.service.FinanceService;
import financehelper.service.WalletService;
import financehelper.ui.ConsoleMenu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            // ==================== СОЗДАНИЕ РЕПОЗИТОРИЕВ ====================

            UserRepository userRepository =
                    new InMemoryUserRepository();

            WalletRepository walletRepository =
                    new InMemoryWalletRepository();

            FinancialOperationRepository operationRepository =
                    new InMemoryFinancialOperationRepository();

            CategoryRepository categoryRepository =
                    new InMemoryCategoryRepository();

            // ==================== СОЗДАНИЕ СЕРВИСОВ ====================

            AuthService authService =
                    new AuthService(userRepository);

            WalletService walletService =
                    new WalletService(walletRepository);

            FinanceService financeService =
                    new FinanceService(
                            walletRepository,
                            operationRepository,
                            categoryRepository
                    );

            ConsoleMenu menu = new ConsoleMenu(scanner);

            // ==================== ГЛАВНЫЙ ЦИКЛ ====================

            boolean running = true;

            while (running) {

                if (authService.isAuthenticated()) {

                    running = handleMainMenu(
                            menu,
                            authService,
                            walletService,
                            financeService
                    );

                } else {

                    running = handleAuthorizationMenu(
                            menu,
                            authService,
                            walletService
                    );
                }
            }

            authService.logout();
            menu.showGoodbye();
        }
    }

    // ==================== АВТОРИЗАЦИЯ И РЕГИСТРАЦИЯ ====================

    private static boolean handleAuthorizationMenu(
            ConsoleMenu menu,
            AuthService authService,
            WalletService walletService
    ) {
        int choice = menu.showAuthorizationMenu();

        switch (choice) {

            case 1 -> handleLogin(
                    menu,
                    authService,
                    walletService
            );

            case 2 -> handleRegistration(
                    menu,
                    authService,
                    walletService
            );

            case 0 -> {
                return false;
            }
        }

        return true;
    }

    // ==================== РЕГИСТРАЦИЯ ====================

    private static void handleRegistration(
            ConsoleMenu menu,
            AuthService authService,
            WalletService walletService
    ) {
        menu.showInformation("Создание нового аккаунта");

        String login = menu.readLogin();
        char[] password = menu.readPassword("Введите пароль: ");

        try {
            User user = authService.register(login, password);

            try {
                Wallet wallet = walletService.createWallet(user.getId());

                menu.showRegistrationSuccess(user, wallet);

            } catch (RuntimeException e) {
                menu.showError(
                        "Аккаунт создан, но кошелёк создать не удалось. " +
                                "При следующем входе приложение повторит попытку. " +
                                "Причина: " + e.getMessage()
                );
            }

        } catch (FinanceException |
                 IllegalArgumentException |
                 IllegalStateException e) {

            menu.showError(e.getMessage());
        }
    }

    // ==================== АВТОРИЗАЦИЯ ====================

    private static void handleLogin(
            ConsoleMenu menu,
            AuthService authService,
            WalletService walletService
    ) {
        menu.showInformation("Вход в аккаунт");

        String login = menu.readLogin();
        char[] password = menu.readPassword("Введите пароль: ");

        try {
            User user = authService.login(login, password);

            Wallet wallet = walletService.getOrCreateWallet(user.getId());

            menu.showLoginSuccess(user, wallet);

        } catch (FinanceException |
                 IllegalArgumentException |
                 IllegalStateException e) {

            authService.logout();
            menu.showError(e.getMessage());
        }
    }

    // ==================== ГЛАВНОЕ МЕНЮ ====================

    private static boolean handleMainMenu(
            ConsoleMenu menu,
            AuthService authService,
            WalletService walletService,
            FinanceService financeService
    ) {
        User currentUser = authService.getCurrentUser().orElse(null);

        if (currentUser == null) {
            return true;
        }

        int choice = menu.showMainMenu(currentUser.getLogin());

        switch (choice) {

            case 1 -> {
                Wallet wallet = walletService.getOrCreateWallet(
                        currentUser.getId()
                );

                BigDecimal balance = financeService.getBalance(
                        wallet.getId()
                );

                menu.showWalletOverview(wallet, balance);
            }

            case 2 -> handleOperationsMenu(
                    menu,
                    currentUser,
                    walletService,
                    financeService
            );

            case 3 -> menu.showFeatureNotImplemented(
                    "Управление категориями"
            );

            case 4 -> menu.showFeatureNotImplemented(
                    "Управление бюджетами"
            );

            case 5 -> menu.showFeatureNotImplemented(
                    "Статистика и аналитика"
            );

            case 6 -> menu.showFeatureNotImplemented(
                    "Переводы между пользователями"
            );

            case 7 -> menu.showFeatureNotImplemented(
                    "Импорт и экспорт данных"
            );

            case 8 -> menu.showHelp();

            case 9 -> {
                authService.logout();
                menu.showLogoutSuccess();
            }

            case 0 -> {
                return false;
            }
        }

        return true;
    }

    // ==================== УПРАВЛЕНИЕ ОПЕРАЦИЯМИ ====================

    private static void handleOperationsMenu(
            ConsoleMenu menu,
            User currentUser,
            WalletService walletService,
            FinanceService financeService
    ) {
        Wallet wallet = walletService.getOrCreateWallet(
                currentUser.getId()
        );

        int choice = menu.showOperationsMenu();

        switch (choice) {

            case 1 -> handleAddOperation(
                    menu,
                    wallet.getId(),
                    OperationType.INCOME,
                    financeService
            );

            case 2 -> handleAddOperation(
                    menu,
                    wallet.getId(),
                    OperationType.EXPENSE,
                    financeService
            );

            case 3 -> menu.showOperationHistory(
                    financeService.getOperations(wallet.getId()),
                    financeService.getAllCategories()
            );

            case 4 -> handleFilterByCategory(
                    menu,
                    wallet.getId(),
                    financeService
            );

            case 5 -> handleFilterByPeriod(
                    menu,
                    wallet.getId(),
                    financeService
            );

            case 6 -> handleEditOperation(
                    menu,
                    wallet.getId(),
                    financeService
            );

            case 7 -> handleDeleteOperation(
                    menu,
                    wallet.getId(),
                    financeService
            );
        }
    }

    // ==================== ДОБАВЛЕНИЕ ДОХОДА ИЛИ РАСХОДА ====================

    private static void handleAddOperation(
            ConsoleMenu menu,
            long walletId,
            OperationType type,
            FinanceService financeService
    ) {
        List<Category> categories = financeService.getCategories(type);

        if (categories.isEmpty()) {
            menu.showError(
                    "Нет доступных категорий для этой операции."
            );
            return;
        }

        menu.showCategoriesForSelection(categories, type);

        Long categoryId = menu.readCategoryId(categories);

        if (categoryId == null) {
            return;
        }

        BigDecimal amount = menu.readAmount();

        if (amount == null) {
            return;
        }

        try {
            FinancialOperation operation;

            if (type == OperationType.INCOME) {
                operation = financeService.addIncome(
                        walletId,
                        amount,
                        categoryId
                );
            } else {
                operation = financeService.addExpense(
                        walletId,
                        amount,
                        categoryId
                );
            }

            BigDecimal balance = financeService.getBalance(walletId);

            String categoryName =
                    financeService.getCategoryName(categoryId);

            menu.showOperationCreated(
                    operation,
                    categoryName,
                    balance
            );

        } catch (FinanceException |
                 IllegalArgumentException |
                 IllegalStateException e) {

            menu.showError(e.getMessage());
        }
    }

    // ==================== ФИЛЬТРАЦИЯ ПО КАТЕГОРИИ ====================

    private static void handleFilterByCategory(
            ConsoleMenu menu,
            long walletId,
            FinanceService financeService
    ) {
        List<Category> categories = financeService.getAllCategories();

        if (categories.isEmpty()) {
            menu.showError("Категории отсутствуют.");
            return;
        }

        menu.showAllCategoriesForSelection(categories);

        Long categoryId = menu.readCategoryId(categories);

        if (categoryId == null) {
            return;
        }

        try {
            List<FinancialOperation> operations =
                    financeService.getOperationsByCategory(
                            walletId,
                            categoryId
                    );

            menu.showOperationHistory(
                    operations,
                    categories
            );

        } catch (FinanceException |
                 IllegalArgumentException |
                 IllegalStateException e) {

            menu.showError(e.getMessage());
        }
    }

    // ==================== ФИЛЬТРАЦИЯ ПО ПЕРИОДУ ====================

    private static void handleFilterByPeriod(
            ConsoleMenu menu,
            long walletId,
            FinanceService financeService
    ) {
        menu.showInformation(
                "Введите даты периода в формате ДД.ММ.ГГГГ. " +
                        "Для отмены введите 0."
        );

        LocalDate from = menu.readDate("Начальная дата: ");

        if (from == null) {
            return;
        }

        LocalDate to = menu.readDate("Конечная дата: ");

        if (to == null) {
            return;
        }

        try {
            List<FinancialOperation> operations =
                    financeService.getOperationsBetween(
                            walletId,
                            from,
                            to
                    );

            menu.showFilterPeriod(from, to);

            menu.showOperationHistory(
                    operations,
                    financeService.getAllCategories()
            );

        } catch (FinanceException |
                 IllegalArgumentException |
                 IllegalStateException e) {

            menu.showError(e.getMessage());
        }
    }

    // ==================== РЕДАКТИРОВАНИЕ ОПЕРАЦИИ ====================

    private static void handleEditOperation(
            ConsoleMenu menu,
            long walletId,
            FinanceService financeService
    ) {
        List<FinancialOperation> operations =
                financeService.getOperations(walletId);

        if (operations.isEmpty()) {
            menu.showInformation(
                    "Нет операций для редактирования."
            );
            return;
        }

        menu.showOperationHistory(
                operations,
                financeService.getAllCategories()
        );

        Long operationId = menu.readPositiveLong(
                "Введите номер операции для редактирования (0 - отмена): "
        );

        if (operationId == null) {
            return;
        }

        try {
            FinancialOperation oldOperation =
                    financeService.getOperation(
                            walletId,
                            operationId
                    );

            menu.showOperationDetails(
                    oldOperation,
                    financeService.getCategoryName(
                            oldOperation.getCategoryId()
                    )
            );

            int editChoice = menu.showEditOperationMenu();

            if (editChoice == 0) {
                return;
            }

            BigDecimal newAmount = oldOperation.getAmount();
            long newCategoryId = oldOperation.getCategoryId();

            // Изменение суммы.
            if (editChoice == 1 || editChoice == 3) {
                BigDecimal amount = menu.readAmount();

                if (amount == null) {
                    return;
                }

                newAmount = amount;
            }

            // Изменение категории.
            if (editChoice == 2 || editChoice == 3) {

                List<Category> categories =
                        financeService.getCategories(
                                oldOperation.getType()
                        );

                menu.showCategoriesForSelection(
                        categories,
                        oldOperation.getType()
                );

                Long categoryId = menu.readCategoryId(categories);

                if (categoryId == null) {
                    return;
                }

                newCategoryId = categoryId;
            }

            FinancialOperation updatedOperation =
                    financeService.updateOperation(
                            walletId,
                            operationId,
                            newAmount,
                            newCategoryId
                    );

            BigDecimal balance = financeService.getBalance(walletId);

            menu.showOperationUpdated(
                    updatedOperation,
                    financeService.getCategoryName(newCategoryId),
                    balance
            );

        } catch (FinanceException |
                 IllegalArgumentException |
                 IllegalStateException e) {

            menu.showError(e.getMessage());
        }
    }

    // ==================== УДАЛЕНИЕ ОПЕРАЦИИ ====================

    private static void handleDeleteOperation(
            ConsoleMenu menu,
            long walletId,
            FinanceService financeService
    ) {
        List<FinancialOperation> operations =
                financeService.getOperations(walletId);

        if (operations.isEmpty()) {
            menu.showInformation(
                    "Нет операций для удаления."
            );
            return;
        }

        menu.showOperationHistory(
                operations,
                financeService.getAllCategories()
        );

        Long operationId = menu.readPositiveLong(
                "Введите номер операции для удаления (0 - отмена): "
        );

        if (operationId == null) {
            return;
        }

        try {
            FinancialOperation operation =
                    financeService.getOperation(
                            walletId,
                            operationId
                    );

            menu.showOperationDetails(
                    operation,
                    financeService.getCategoryName(
                            operation.getCategoryId()
                    )
            );

            boolean confirmed = menu.readConfirmation(
                    "Вы действительно хотите удалить эту операцию? (да/нет): "
            );

            if (!confirmed) {
                menu.showInformation("Удаление отменено.");
                return;
            }

            FinancialOperation deletedOperation =
                    financeService.deleteOperation(
                            walletId,
                            operationId
                    );

            BigDecimal balance = financeService.getBalance(walletId);

            menu.showOperationDeleted(
                    deletedOperation,
                    balance
            );

        } catch (FinanceException |
                 IllegalArgumentException |
                 IllegalStateException e) {

            menu.showError(e.getMessage());
        }
    }
}
