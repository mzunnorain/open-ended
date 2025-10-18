package bank;

import java.util.*;

import bank.accounts.BankAccount;
import bank.accounts.CheckingAccount;
import bank.accounts.SavingsAccount;
import bank.enums.AccountStatus;
import bank.enums.TransactionStatus;
import bank.enums.TransactionType;
import bank.exceptions.InvalidAccountException;

/**
 * Central repository and orchestrator for Customers and BankAccounts.
 */
public class Bank {
    private final Map<String, Customer> customersById = new HashMap<>();
    private final Map<String, BankAccount> accountsByNumber = new HashMap<>();

    // Admin credentials (simple, per spec)
    private final String adminUser = "admin";
    private final String adminPass = "password";

    // ATM login policy
    private final int maxFailedPinAttempts = 3;

    public Bank() {
        seedSampleData();
    }

    public boolean isValidAdmin(String user, String pass) {
        return adminUser.equals(user) && adminPass.equals(pass);
    }

    public Customer getCustomer(String customerId) {
        return customersById.get(customerId);
    }

    public BankAccount getAccount(String accountNumber) {
        return accountsByNumber.get(accountNumber);
    }

    public Collection<Customer> getAllCustomers() {
        return customersById.values();
    }

    public Collection<BankAccount> getAllAccounts() {
        return accountsByNumber.values();
    }

    public void registerCustomer(Customer c) {
        customersById.put(c.getCustomerId(), c);
    }

    public String createSavingsAccount(String customerId, double initial, double minBalance) {
        String acc = generateAccountNumber();
        SavingsAccount sa = new SavingsAccount(acc, customerId, initial, minBalance);
        accountsByNumber.put(acc, sa);
        Customer cust = customersById.get(customerId);
        if (cust != null) cust.addAccount(sa);
        return acc;
    }

    public String createCheckingAccount(String customerId, double initial, double overdraftLimit) {
        String acc = generateAccountNumber();
        CheckingAccount ca = new CheckingAccount(acc, customerId, initial, overdraftLimit);
        accountsByNumber.put(acc, ca);
        Customer cust = customersById.get(customerId);
        if (cust != null) cust.addAccount(ca);
        return acc;
    }

    // Authentication with lockout
    public Customer authenticate(String customerId, String pin) {
        Customer c = customersById.get(customerId);
        if (c == null) return null;
        if (c.isBlocked()) return null;
        if (Objects.equals(c.getPin(), pin)) {
            c.resetFailedPinAttempts();
            return c;
        } else {
            c.incrementFailedPinAttempts();
            if (c.getFailedPinAttempts() >= maxFailedPinAttempts) {
                c.setBlocked(true);
            }
            return null;
        }
    }

    public boolean unblockCustomer(String customerId) {
        Customer c = customersById.get(customerId);
        if (c == null) return false;
        c.setBlocked(false);
        c.resetFailedPinAttempts();
        return true;
    }

    // Transfers
    public String transfer(String sourceAccNo, String destAccNo, double amount) throws InvalidAccountException {
        BankAccount src = accountsByNumber.get(sourceAccNo);
        BankAccount dst = accountsByNumber.get(destAccNo);
        if (src == null || dst == null) throw new InvalidAccountException("Invalid source or destination account.");
        if (src.getStatus() != AccountStatus.ACTIVE || dst.getStatus() != AccountStatus.ACTIVE) {
            return "Either source or destination account is not ACTIVE.";
        }
        if (amount <= 0) {
            return "Amount must be positive.";
        }
        if (!src.canWithdrawAmount(amount)) {
            // record failed transfer on src
            Transaction tfail = new Transaction(TransactionType.TRANSFER, amount, sourceAccNo, destAccNo,
                    TransactionStatus.FAILED_INSUFFICIENT_FUNDS);
            src.addTransaction(tfail);
            return "Insufficient funds or rule violation for source account.";
        }
        // perform
        src.adjustBalance(-amount);
        dst.adjustBalance(+amount);
        Transaction t = new Transaction(TransactionType.TRANSFER, amount, sourceAccNo, destAccNo, TransactionStatus.SUCCESS);
        src.addTransaction(t);
        dst.addTransaction(t);
        return "Transfer successful. New source balance: " + String.format("%.2f", src.getBalance());
    }

    private String generateAccountNumber() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void seedSampleData() {
        // Customers
        Customer a = new Customer("C001", "Alice", "1111");
        Customer b = new Customer("C002", "Bob", "2222");
        registerCustomer(a);
        registerCustomer(b);
        // Accounts
        createSavingsAccount("C001", 5000.0, 1000.0);
        createCheckingAccount("C001", 2000.0, 500.0);
        createCheckingAccount("C002", 1500.0, 300.0);
    }
}
