
package financehelper.ui;

import java.util.Scanner;

public class ConsoleMenu {

    private final Scanner scanner;

    public ConsoleMenu(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Сканер ввода не может быть пустым.");
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

    // ==================== ПЕРЕВОДЫ МЕЖДУ ПОЛЬЗОВАТЕЛЯМИ ====================

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

    // ==================== СПРАВКА ====================

    public void showHelp() {
        printHeader("СПРАВКА");

        System.out.println("FINANCEHELPER - приложение для управления личными финансами.");
        System.out.println();
        System.out.println("Основные возможности:");
        System.out.println("- Управление доходами и расходами.");
        System.out.println("- Создание и редактирование категорий.");
        System.out.println("- Контроль расходов и бюджетов.");
        System.out.println("- Просмотр статистики и истории операций.");
        System.out.println("- Переводы между пользователями.");
        System.out.println("- Импорт и экспорт финансовых данных.");
        System.out.println();
        System.out.println("Для выбора действия в меню введите его номер.");
        System.out.println("Для возврата в предыдущее меню используйте пункт 0.");
        System.out.println();

        waitForEnter();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

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

                System.out.println("Ошибка: выберите число от " + min + " до " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: необходимо ввести целое число.");
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
