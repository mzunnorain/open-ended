package bank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import bank.enums.TransactionStatus;
import bank.enums.TransactionType;

/**
 * Records comprehensive details of financial operations.
 */
public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String sourceAccount;
    private final String destinationAccount;
    private final TransactionStatus status;

    public Transaction(TransactionType type, double amount, String sourceAccount, String destinationAccount,
            TransactionStatus status) {
        this.transactionId = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.status = status;
    }

    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSourceAccount() { return sourceAccount; }
    public String getDestinationAccount() { return destinationAccount; }
    public TransactionStatus getStatus() { return status; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %s | Type: %s | Amount: %.2f | From: %s | To: %s | Status: %s",
                timestamp.format(fmt), transactionId, type, amount,
                sourceAccount == null ? "-" : sourceAccount,
                destinationAccount == null ? "-" : destinationAccount,
                status);
    }

    public String receipt(double newBalance) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "\n===== RECEIPT =====\n" +
                "Transaction ID: " + transactionId + "\n" +
                "Type: " + type + "\n" +
                "Amount: " + String.format("%.2f", amount) + "\n" +
                "From: " + (sourceAccount == null ? "-" : sourceAccount) + "\n" +
                "To: " + (destinationAccount == null ? "-" : destinationAccount) + "\n" +
                "Status: " + status + "\n" +
                "Timestamp: " + timestamp.format(fmt) + "\n" +
                "New Balance: " + String.format("%.2f", newBalance) + "\n" +
                "===================\n";
    }
}
