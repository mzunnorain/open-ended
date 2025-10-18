package bank;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        Scanner scanner = new Scanner(System.in);
        ATM atm = new ATM(bank, scanner);
        AdminConsole admin = new AdminConsole(bank, scanner);
        while (true) {
            System.out.println("\n=== Banking System ===");
            System.out.println("1) ATM (Customer)");
            System.out.println("2) Admin Console");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1":
                    atm.start();
                    break;
                case "2":
                    admin.start();
                    break;
                case "0":
                    System.out.println("Goodbye.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
