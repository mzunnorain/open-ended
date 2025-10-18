package bank.accounts;

public class CheckingAccount extends BankAccount {
    private final double overdraftLimit; // positive number, allowed negative balance down to -overdraftLimit

    public CheckingAccount(String accountNumber, String customerId, double initialBalance, double overdraftLimit) {
        super(accountNumber, customerId, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    protected boolean canWithdraw(double amount) {
        return (balance - amount) >= -overdraftLimit;
    }
}
