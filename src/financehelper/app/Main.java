package financehelper.app;

import financehelper.ui.ConsoleMenu;

import java.util.Scanner;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ConsoleMenu menu = new ConsoleMenu(scanner);

        boolean running = true;

        while (running) {
            int choice = menu.showAuthorizationMenu();

            switch (choice) {
                case 1 -> System.out.println("Авторизация пока не реализована.");
                case 2 -> System.out.println("Регистрация пока не реализована.");
                case 0 -> running = false;
            }
        }

        System.out.println("Работа FinanceHelper завершена.");
    }
}