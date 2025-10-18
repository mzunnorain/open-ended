package bank;

import java.util.List;
import java.util.Scanner;

import bank.accounts.BankAccount;
import bank.enums.TransactionStatus;
import bank.enums.TransactionType;
import bank.exceptions.InsufficientFundsException;
import bank.exceptions.InvalidAccountException;

public class ATM {
    private final Bank bank;
    private final Scanner scanner;

    public ATM(Bank bank, Scanner scanner) {
        this.bank = bank;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== ATM Login ===");
            System.out.print("Customer ID (or 'q' to quit): ");
            String id = scanner.nextLine().trim();
            if (id.equalsIgnoreCase("q")) return;
            System.out.print("PIN: ");
            String pin = scanner.nextLine().trim();

            Customer c = bank.authenticate(id, pin);
            if (c == null) {
                Customer existing = bank.getCustomer(id);
                if (existing != null && existing.isBlocked()) {
                    System.out.println("Account blocked due to repeated failed attempts.");
                } else {
                    System.out.println("Invalid credentials.");
                }
                continue;
            }
            System.out.println("Welcome, " + c.getName() + "!");
            customerSession(c);
        }
    }

    private void customerSession(Customer c) {
        BankAccount current = selectAccount(c);
        if (current == null) return; // back
        while (true) {
            System.out.println("\n=== ATM Menu (" + current.getClass().getSimpleName() + " " + current.getAccountNumber() + ") ===");
            System.out.println("1) Check Balance");
            System.out.println("2) Deposit");
            System.out.println("3) Withdraw");
            System.out.println("4) View Transaction History");
            System.out.println("5) Transfer (own or to another customer)");
            System.out.println("6) Switch Account");
            System.out.println("0) Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.printf("Current Balance: %.2f\n", current.getBalance());
                    break;
                case "2":
                    System.out.print("Amount to deposit: ");
                    double dAmt = parseAmount();
                    current.deposit(dAmt);
                    break;
                case "3":
                    System.out.print("Amount to withdraw: ");
                    double wAmt = parseAmount();
                    try {
                        current.withdraw(wAmt);
                    } catch (InsufficientFundsException e) {
                        System.out.println("Withdrawal failed: " + e.getMessage());
                    }
                    break;
                case "4":
                    List<bank.Transaction> txs = current.getTransactions();
                    if (txs.isEmpty()) {
                        System.out.println("No transactions yet.");
                    } else {
                        txs.forEach(t -> System.out.println(t.toString()));
                    }
                    break;
                case "5":
                    doTransfer(c, current);
                    break;
                case "6":
                    current = selectAccount(c);
                    if (current == null) return; // back
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void doTransfer(Customer c, BankAccount current) {
        System.out.println("\nTransfer Options:");
        System.out.println("1) To my other account");
        System.out.println("2) To another customer's account");
        System.out.print("Choose: ");
        String kind = scanner.nextLine().trim();
        System.out.print("Amount: ");
        double amt = parseAmount();
        String destAcc;
        if ("1".equals(kind)) {
            BankAccount dest = selectOtherOwnAccount(c, current.getAccountNumber());
            if (dest == null) return;
            destAcc = dest.getAccountNumber();
        } else if ("2".equals(kind)) {
            System.out.print("Destination account number: ");
            destAcc = scanner.nextLine().trim();
        } else {
            System.out.println("Invalid option.");
            return;
        }
        try {
            // Perform transfer at bank-level to ensure consistent rules
            String msg = bank.transfer(current.getAccountNumber(), destAcc, amt);
            System.out.println(msg);
            // If successful, print a simple receipt using a synthetic transaction for display
            if (msg.startsWith("Transfer successful")) {
                bank.Transaction receiptTx = new bank.Transaction(TransactionType.TRANSFER, amt, current.getAccountNumber(), destAcc, TransactionStatus.SUCCESS);
                System.out.print(receiptTx.receipt(current.getBalance()));
            }
        } catch (InvalidAccountException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private BankAccount selectAccount(Customer c) {
        List<BankAccount> accs = c.getAccounts();
        if (accs.isEmpty()) {
            System.out.println("You have no accounts.");
            return null;
        }
        while (true) {
            System.out.println("\nSelect Account:");
            for (int i = 0; i < accs.size(); i++) {
                BankAccount a = accs.get(i);
                System.out.printf("%d) %s %s (Balance: %.2f)\n", i + 1, a.getClass().getSimpleName(), a.getAccountNumber(), a.getBalance());
            }
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String ch = scanner.nextLine().trim();
            if ("0".equals(ch)) return null;
            try {
                int idx = Integer.parseInt(ch) - 1;
                if (idx >= 0 && idx < accs.size()) return accs.get(idx);
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid selection.");
        }
    }

    private BankAccount selectOtherOwnAccount(Customer c, String currentAcc) {
        List<BankAccount> accs = c.getAccounts();
        int count = 0;
        for (BankAccount a : accs) if (!a.getAccountNumber().equals(currentAcc)) count++;
        if (count == 0) {
            System.out.println("No other accounts available.");
            return null;
        }
        while (true) {
            System.out.println("\nSelect Destination Account:");
            int display = 1;
            for (BankAccount a : accs) {
                if (a.getAccountNumber().equals(currentAcc)) continue;
                System.out.printf("%d) %s %s (Balance: %.2f)\n", display++, a.getClass().getSimpleName(), a.getAccountNumber(), a.getBalance());
            }
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String ch = scanner.nextLine().trim();
            if ("0".equals(ch)) return null;
            try {
                int idxWanted = Integer.parseInt(ch);
                display = 1;
                for (BankAccount a : accs) {
                    if (a.getAccountNumber().equals(currentAcc)) continue;
                    if (display == idxWanted) return a;
                    display++;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid selection.");
        }
    }

    private double parseAmount() {
        while (true) {
            String s = scanner.nextLine().trim();
            try {
                double v = Double.parseDouble(s);
                return v;
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}
