
package financehelper.ui;

import financehelper.model.Category;
import financehelper.model.FinancialOperation;
import financehelper.model.OperationType;
import financehelper.model.User;
import financehelper.model.Wallet;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final Scanner scanner;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public ConsoleMenu(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException(
                    "Сканер ввода не может быть пустым."
            );
        }

        this.scanner = scanner;
    }

    // ==================== МЕНЮ АВТОРИЗАЦИИ ====================

    public int showAuthorizationMenu() {
        printHeader("FINANCEHELPER");

        System.out.println("Управление личными финансами");
        System.out.println();
        System.out.println("1. Войти в аккаунт");
        System.out.println("2. Зарегистрироваться");
        System.out.println("0. Завершить программу");
        System.out.println();

        return readChoice("Выберите действие: ", 0, 2);
    }

    // ==================== ГЛАВНОЕ МЕНЮ ====================

    public int showMainMenu(String login) {
        printHeader("FINANCEHELPER");

        System.out.println("Пользователь: " + login);
        System.out.println();
        System.out.println("1. Обзор финансов");
        System.out.println("2. Управление операциями");
        System.out.println("3. Управление категориями");
        System.out.println("4. Управление бюджетами");
        System.out.println("5. Статистика и аналитика");
        System.out.println("6. Переводы между пользователями");
        System.out.println("7. Импорт и экспорт данных");
        System.out.println("8. Справка");
        System.out.println("9. Выйти из аккаунта");
        System.out.println("0. Завершить программу");
        System.out.println();

        return readChoice("Выберите действие: ", 0, 9);
    }

    // ==================== УПРАВЛЕНИЕ ОПЕРАЦИЯМИ ====================

    public int showOperationsMenu() {
        printHeader("УПРАВЛЕНИЕ ОПЕРАЦИЯМИ");

        System.out.println("1. Добавить доход");
        System.out.println("2. Добавить расход");
        System.out.println("3. История операций");
        System.out.println("4. Найти операции по категории");
        System.out.println("5. Фильтр операций по периоду");
        System.out.println("6. Редактировать операцию");
        System.out.println("7. Удалить операцию");
        System.out.println("0. Назад");
        System.out.println();

        return readChoice("Выберите действие: ", 0, 7);
    }

    // ==================== УПРАВЛЕНИЕ КАТЕГОРИЯМИ ====================

    public int showCategoriesMenu() {
        printHeader("УПРАВЛЕНИЕ КАТЕГОРИЯМИ");

        System.out.println("1. Показать категории доходов");
        System.out.println("2. Показать категории расходов");
        System.out.println("3. Создать категорию");
        System.out.println("4. Изменить название категории");
        System.out.println("5. Архивировать категорию");
        System.out.println("0. Назад");
        System.out.println();

        return readChoice("Выберите действие: ", 0, 5);
    }

    // ==================== УПРАВЛЕНИЕ БЮДЖЕТАМИ ====================

    public int showBudgetsMenu() {
        printHeader("УПРАВЛЕНИЕ БЮДЖЕТАМИ");

        System.out.println("1. Показать бюджеты");
        System.out.println("2. Установить бюджет");
        System.out.println("3. Изменить бюджет");
        System.out.println("4. Удалить бюджет");
        System.out.println("0. Назад");
        System.out.println();

        return readChoice("Выберите действие: ", 0, 4);
    }

    // ==================== СТАТИСТИКА И АНАЛИТИКА ====================

    public int showStatisticsMenu() {
        printHeader("СТАТИСТИКА И АНАЛИТИКА");

        System.out.println("1. Общая статистика");
        System.out.println("2. Доходы по категориям");
        System.out.println("3. Расходы по категориям");
        System.out.println("4. Статистика по выбранным категориям");
        System.out.println("5. Статистика за период");
        System.out.println("6. Сравнение периодов");
        System.out.println("0. Назад");
        System.out.println();

        return readChoice("Выберите действие: ", 0, 6);
    }

    // ==================== ПЕРЕВОДЫ ====================

    public int showTransfersMenu() {
        printHeader("ПЕРЕВОДЫ");

        System.out.println("1. Отправить перевод");
        System.out.println("2. История переводов");
        System.out.println("0. Назад");
        System.out.println();

        return readChoice("Выберите действие: ", 0, 2);
    }

    // ==================== ИМПОРТ И ЭКСПОРТ ====================

    public int showDataMenu() {
        printHeader("ИМПОРТ И ЭКСПОРТ ДАННЫХ");

        System.out.println("1. Экспорт операций в CSV");
        System.out.println("2. Экспорт данных в JSON");
        System.out.println("3. Импорт данных из JSON");
        System.out.println("0. Назад");
        System.out.println();

        return readChoice("Выберите действие: ", 0, 3);
    }

    // ==================== ВВОД УЧЁТНЫХ ДАННЫХ ====================

    public String readLogin() {
        System.out.print("Введите логин: ");

        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine().trim();
    }

    public char[] readPassword(String message) {
        System.out.print(message);

        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine().toCharArray();
    }

    // ==================== ВВОД ДАННЫХ ОПЕРАЦИИ ====================

    public void showCategoriesForSelection(
            List<Category> categories,
            OperationType type
    ) {
        printHeader(
                type == OperationType.INCOME
                        ? "КАТЕГОРИИ ДОХОДОВ"
                        : "КАТЕГОРИИ РАСХОДОВ"
        );

        if (categories == null || categories.isEmpty()) {
            System.out.println("Доступные категории отсутствуют.");
            System.out.println();
            return;
        }

        for (Category category : categories) {
            System.out.printf(
                    "%d. %s%n",
                    category.getId(),
                    category.getName()
            );
        }

        System.out.println();
    }

    public Long readCategoryId(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        while (true) {
            System.out.print("Введите номер категории: ");

            if (!scanner.hasNextLine()) {
                return null;
            }

            String input = scanner.nextLine().trim();

            try {
                long categoryId = Long.parseLong(input);

                boolean exists = categories.stream()
                        .anyMatch(category ->
                                category.getId() == categoryId
                        );

                if (exists) {
                    return categoryId;
                }

                System.out.println(
                        "Ошибка: выберите категорию из списка."
                );

            } catch (NumberFormatException e) {
                System.out.println(
                        "Ошибка: необходимо ввести целое число."
                );
            }
        }
    }

    public BigDecimal readAmount() {
        while (true) {
            System.out.print("Введите сумму: ");

            if (!scanner.hasNextLine()) {
                return null;
            }

            String input = scanner.nextLine()
                    .trim()
                    .replace(',', '.');

            try {
                BigDecimal amount = new BigDecimal(input);

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println(
                            "Ошибка: сумма должна быть больше нуля."
                    );
                    continue;
                }

                if (amount.stripTrailingZeros().scale() > 2) {
                    System.out.println(
                            "Ошибка: допускается не более двух знаков после запятой."
                    );
                    continue;
                }

                return amount;

            } catch (NumberFormatException e) {
                System.out.println(
                        "Ошибка: введите корректную денежную сумму."
                );
            }
        }
    }

    // ==================== РЕЗУЛЬТАТЫ РАБОТЫ С ОПЕРАЦИЯМИ ====================

    public void showOperationCreated(
            FinancialOperation operation,
            String categoryName,
            BigDecimal balance
    ) {
        printHeader("ОПЕРАЦИЯ ДОБАВЛЕНА");

        String operationName =
                operation.getType() == OperationType.INCOME
                        ? "Доход"
                        : "Расход";

        System.out.println("Тип операции: " + operationName);
        System.out.println("Номер операции: " + operation.getId());
        System.out.println("Категория: " + categoryName);
        System.out.println(
                "Сумма: " + formatAmount(operation.getAmount())
        );
        System.out.println(
                "Новый баланс: " + formatAmount(balance)
        );
        System.out.println();
    }

    public void showOperationHistory(
            List<FinancialOperation> operations,
            List<Category> categories
    ) {
        printHeader("ИСТОРИЯ ОПЕРАЦИЙ");

        if (operations == null || operations.isEmpty()) {
            System.out.println("История операций пока пуста.");
            System.out.println();
            return;
        }

        for (FinancialOperation operation : operations) {

            String operationName =
                    operation.getType() == OperationType.INCOME
                            ? "Доход"
                            : "Расход";

            String categoryName = categories.stream()
                    .filter(category ->
                            category.getId() == operation.getCategoryId()
                    )
                    .map(Category::getName)
                    .findFirst()
                    .orElse(
                            "Категория #" + operation.getCategoryId()
                    );

            System.out.println("----------------------------------------");
            System.out.println("Операция №" + operation.getId());
            System.out.println("Тип: " + operationName);
            System.out.println("Категория: " + categoryName);
            System.out.println(
                    "Сумма: " + formatAmount(operation.getAmount())
            );
            System.out.println(
                    "Дата: " + operation.getCreatedAt().format(DATE_FORMAT)
            );
        }

        System.out.println("----------------------------------------");
        System.out.println();
    }

    // ==================== РЕЗУЛЬТАТЫ АВТОРИЗАЦИИ ====================

    public void showRegistrationSuccess(User user, Wallet wallet) {
        printHeader("РЕГИСТРАЦИЯ ЗАВЕРШЕНА");

        System.out.println("Аккаунт успешно создан!");
        System.out.println("Логин: " + user.getLogin());
        System.out.println("Номер пользователя: " + user.getId());
        System.out.println();
        System.out.println("Для аккаунта создан кошелёк.");
        System.out.println("Номер кошелька: " + wallet.getId());
        System.out.println();
        System.out.println("Теперь вы можете войти в свой аккаунт.");
        System.out.println();
    }

    public void showLoginSuccess(User user, Wallet wallet) {
        printHeader("АВТОРИЗАЦИЯ");

        System.out.println("Вход выполнен успешно!");
        System.out.println("Добро пожаловать, " + user.getLogin() + "!");
        System.out.println("Номер пользователя: " + user.getId());
        System.out.println("Номер кошелька: " + wallet.getId());
        System.out.println();
    }

    public void showLogoutSuccess() {
        System.out.println();
        System.out.println("Вы вышли из аккаунта.");
        System.out.println();
    }

    // ==================== ОБЗОР КОШЕЛЬКА ====================

    public void showWalletOverview(
            Wallet wallet,
            BigDecimal balance
    ) {
        printHeader("МОИ ФИНАНСЫ");

        System.out.println("Номер кошелька: " + wallet.getId());
        System.out.println(
                "Дата создания: " + wallet.getCreatedAt().format(DATE_FORMAT)
        );
        System.out.println();
        System.out.println(
                "Текущий баланс: " + formatAmount(balance)
        );
        System.out.println();
    }

    // ==================== СПРАВКА ====================

    public void showHelp() {
        printHeader("СПРАВКА");

        System.out.println(
                "FINANCEHELPER - приложение для управления личными финансами."
        );
        System.out.println();
        System.out.println("Основные возможности:");
        System.out.println("- Регистрация и авторизация пользователей.");
        System.out.println("- Управление доходами и расходами.");
        System.out.println("- Создание и редактирование категорий.");
        System.out.println("- Контроль расходов и бюджетов.");
        System.out.println("- Просмотр статистики и истории операций.");
        System.out.println("- Переводы между пользователями.");
        System.out.println("- Импорт и экспорт финансовых данных.");
        System.out.println();
        System.out.println("Для выбора действия введите его номер.");
        System.out.println("Для возврата в предыдущее меню используйте пункт 0.");
        System.out.println();

        waitForEnter();
    }

    // ==================== СООБЩЕНИЯ ====================

    public void showError(String message) {
        System.out.println();
        System.out.println("Ошибка: " + message);
        System.out.println();
    }

    public void showInformation(String message) {
        System.out.println();
        System.out.println(message);
        System.out.println();
    }

    public void showFeatureNotImplemented(String feature) {
        showInformation(
                "Раздел \"" + feature + "\" пока находится в разработке."
        );
    }

    public void showGoodbye() {
        System.out.println();
        System.out.println("Работа FinanceHelper завершена.");
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private String formatAmount(BigDecimal amount) {
        return amount
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .toPlainString() + " ₽";
    }

    private void printHeader(String title) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("          " + title);
        System.out.println("========================================");
        System.out.println();
    }

    private int readChoice(String message, int min, int max) {
        while (true) {
            System.out.print(message);

            if (!scanner.hasNextLine()) {
                System.out.println("\nВвод завершён.");
                return 0;
            }

            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);

                if (choice >= min && choice <= max) {
                    return choice;
                }

                System.out.println(
                        "Ошибка: выберите число от " + min + " до " + max + "."
                );

            } catch (NumberFormatException e) {
                System.out.println(
                        "Ошибка: необходимо ввести целое число."
                );
            }
        }
    }

    private void waitForEnter() {
        System.out.println("Нажмите Enter, чтобы вернуться в меню...");

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}
