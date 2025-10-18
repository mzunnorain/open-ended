package bank.accounts;

public class SavingsAccount extends BankAccount {
    private final double minimumBalance;

    public SavingsAccount(String accountNumber, String customerId, double initialBalance, double minimumBalance) {
        super(accountNumber, customerId, initialBalance);
        this.minimumBalance = minimumBalance;
    }

    @Override
    protected boolean canWithdraw(double amount) {
        return (balance - amount) >= minimumBalance;
    }
}
