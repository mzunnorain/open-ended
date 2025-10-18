package bank;

import java.util.ArrayList;
import java.util.List;

import bank.accounts.BankAccount;

/**
 * Represents a bank customer with login credentials (customerId + PIN)
 * and a collection of accounts.
 */
public class Customer {
    private final String customerId;
    private final String name;
    private String pin;
    private boolean blocked;
    private int failedPinAttempts;
    private final List<BankAccount> accounts = new ArrayList<>();

    public Customer(String customerId, String name, String pin) {
        this.customerId = customerId;
        this.name = name;
        this.pin = pin;
        this.blocked = false;
        this.failedPinAttempts = 0;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public int getFailedPinAttempts() { return failedPinAttempts; }
    public void resetFailedPinAttempts() { this.failedPinAttempts = 0; }
    public void incrementFailedPinAttempts() { this.failedPinAttempts++; }

    public List<BankAccount> getAccounts() { return accounts; }
    public void addAccount(BankAccount account) { accounts.add(account); }
}
