package bank.accounts;

import java.util.ArrayList;
import java.util.List;

import bank.Transaction;
import bank.enums.AccountStatus;
import bank.enums.TransactionStatus;
import bank.enums.TransactionType;
import bank.exceptions.InsufficientFundsException;

public abstract class BankAccount {
    protected final String accountNumber;
    protected final String customerId;
    protected double balance;
    protected AccountStatus status = AccountStatus.ACTIVE;
    protected final List<Transaction> transactions = new ArrayList<>();

    protected BankAccount(String accountNumber, String customerId, double initialBalance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = initialBalance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId() { return customerId; }
    public double getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public List<Transaction> getTransactions() { return transactions; }

    public void deposit(double amount) {
        if (amount <= 0) {
            transactions.add(new Transaction(TransactionType.DEPOSIT, amount, null, accountNumber, TransactionStatus.FAILED));
            return;
        }
        balance += amount;
        Transaction t = new Transaction(TransactionType.DEPOSIT, amount, null, accountNumber, TransactionStatus.SUCCESS);
        transactions.add(t);
        System.out.print(t.receipt(balance));
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            transactions.add(new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.FAILED));
            return;
        }
        if (!canWithdraw(amount)) {
            Transaction t = new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null,
                    TransactionStatus.FAILED_INSUFFICIENT_FUNDS);
            transactions.add(t);
            throw new InsufficientFundsException("Withdrawal would violate account rules.");
        }
        balance -= amount;
        Transaction t = new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.SUCCESS);
        transactions.add(t);
        System.out.print(t.receipt(balance));
    }

    protected abstract boolean canWithdraw(double amount);

    // Helper methods for bank-managed transfers
    public boolean canWithdrawAmount(double amount) {
        return canWithdraw(amount);
    }

    public void adjustBalance(double delta) {
        this.balance += delta;
    }

    public void addTransaction(Transaction t) {
        this.transactions.add(t);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "accountNumber='" + accountNumber + '\'' +
                ", customerId='" + customerId + '\'' +
                ", balance=" + String.format("%.2f", balance) +
                ", status=" + status +
                '}';
    }
}
