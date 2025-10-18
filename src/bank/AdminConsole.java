package bank;

import java.util.Scanner;

import bank.accounts.BankAccount;

public class AdminConsole {
    private final Bank bank;
    private final Scanner scanner;

    public AdminConsole(Bank bank, Scanner scanner) {
        this.bank = bank;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("\n=== Admin Login ===");
        System.out.print("Username: ");
        String u = scanner.nextLine().trim();
        System.out.print("Password: ");
        String p = scanner.nextLine().trim();
        if (!bank.isValidAdmin(u, p)) {
            System.out.println("Invalid admin credentials.");
            return;
        }
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1) View All Customers");
            System.out.println("2) View All Accounts");
            System.out.println("3) Create New Account for Existing Customer");
            System.out.println("4) Unblock Customer Login");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1":
                    for (Customer c : bank.getAllCustomers()) {
                        System.out.print("Customer " + c.getCustomerId() + ": " + c.getName() + " | Accounts: ");
                        StringBuilder sb = new StringBuilder();
                        for (var a : c.getAccounts()) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(a.getAccountNumber());
                        }
                        System.out.println(sb);
                    }
                    break;
                case "2":
                    for (BankAccount a : bank.getAllAccounts()) {
                        System.out.println(a.toString());
                    }
                    break;
                case "3":
                    System.out.print("Customer ID: ");
                    String cid = scanner.nextLine().trim();
                    Customer c = bank.getCustomer(cid);
                    if (c == null) {
                        System.out.println("No such customer.");
                        break;
                    }
                    System.out.println("1) Savings  2) Checking");
                    System.out.print("Type: ");
                    String type = scanner.nextLine().trim();
                    System.out.print("Initial Balance: ");
                    double initial = parseDouble();
                    if ("1".equals(type)) {
                        System.out.print("Minimum Balance: ");
                        double min = parseDouble();
                        String acc = bank.createSavingsAccount(cid, initial, min);
                        System.out.println("Created SavingsAccount: " + acc);
                    } else if ("2".equals(type)) {
                        System.out.print("Overdraft Limit: ");
                        double od = parseDouble();
                        String acc = bank.createCheckingAccount(cid, initial, od);
                        System.out.println("Created CheckingAccount: " + acc);
                    } else {
                        System.out.println("Invalid type.");
                    }
                    break;
                case "4":
                    System.out.print("Customer ID to unblock: ");
                    String uc = scanner.nextLine().trim();
                    boolean ok = bank.unblockCustomer(uc);
                    System.out.println(ok ? "Unblocked." : "Customer not found.");
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private double parseDouble() {
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}
