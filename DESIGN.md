# Advanced Object-Oriented System Design & Data Management – Banking System (CLI)

## Overview
A Java CLI application modeling a bank with customers, multiple account types, transaction logging, ATM (customer) and Admin consoles. Emphasis on OOP architecture, data structures, error handling, and extendability.

Entry point: `src/bank/Main.java`.

Seed data: Customers `C001` (PIN `1111`) and `C002` (PIN `2222`) with sample accounts.

---

## UML-like Class Diagram (Text-Based)
```
Bank
- customersById: Map<String, Customer>
- accountsByNumber: Map<String, BankAccount>
- adminUser: String
- adminPass: String
- maxFailedPinAttempts: int
+ Bank()
+ isValidAdmin(user, pass): boolean
+ getCustomer(id): Customer
+ getAccount(accNo): BankAccount
+ getAllCustomers(): Collection<Customer>
+ getAllAccounts(): Collection<BankAccount>
+ registerCustomer(c: Customer): void
+ createSavingsAccount(customerId, initial, minBalance): String
+ createCheckingAccount(customerId, initial, overdraftLimit): String
+ authenticate(customerId, pin): Customer
+ unblockCustomer(customerId): boolean
+ transfer(sourceAccNo, destAccNo, amount): String

Customer
- customerId: String
- name: String
- pin: String
- blocked: boolean
- failedPinAttempts: int
- accounts: List<BankAccount>
+ getCustomerId(): String
+ getName(): String
+ getPin()/setPin()
+ isBlocked()/setBlocked()
+ resetFailedPinAttempts()/incrementFailedPinAttempts()
+ getAccounts(): List<BankAccount>
+ addAccount(a: BankAccount): void

BankAccount (abstract)
- accountNumber: String
- customerId: String
- balance: double
- status: AccountStatus
- transactions: List<Transaction>
# canWithdraw(amount): boolean
+ deposit(amount): void
+ withdraw(amount): void throws InsufficientFundsException
+ canWithdrawAmount(amount): boolean
+ adjustBalance(delta): void
+ addTransaction(t: Transaction): void

SavingsAccount : BankAccount
- minimumBalance: double
# canWithdraw(amount): boolean  (balance - amount >= minimumBalance)

CheckingAccount : BankAccount
- overdraftLimit: double
# canWithdraw(amount): boolean  (balance - amount >= -overdraftLimit)

Transaction
- transactionId: String (UUID)
- type: TransactionType
- amount: double
- timestamp: LocalDateTime
- sourceAccount: String
- destinationAccount: String
- status: TransactionStatus
+ receipt(newBalance): String

ATM (CLI)
- bank: Bank
- scanner: Scanner
+ start(): void  (login, account selection, menu)

AdminConsole (CLI)
- bank: Bank
- scanner: Scanner
+ start(): void (admin menu)

Enums: TransactionType {DEPOSIT, WITHDRAWAL, TRANSFER}
       TransactionStatus {SUCCESS, FAILED_INSUFFICIENT_FUNDS, FAILED_INVALID_ACCOUNT, FAILED_BLOCKED_ACCOUNT, FAILED_AUTHENTICATION, FAILED}
       AccountStatus {ACTIVE, BLOCKED, CLOSED}

Exceptions: InsufficientFundsException, InvalidAccountException, AccountBlockedException, AuthenticationException
```

Relationships:
- Bank aggregates `Customer` and `BankAccount` via maps.
- `Customer` aggregates `BankAccount` via list.
- `BankAccount` aggregates `Transaction` via list.
- ATM/AdminConsole interact only with `Bank` (abstraction boundary).
- Inheritance: `SavingsAccount`, `CheckingAccount` extend `BankAccount`.

---

## Data Structure Justification
- Bank.customersById: `HashMap<String, Customer>` for O(1) customer lookup by ID.
- Bank.accountsByNumber: `HashMap<String, BankAccount>` for O(1) account lookup by account number (used by transfers and admin queries).
- Customer.accounts: `ArrayList<BankAccount>` preserves insertion order and allows iteration and selection in CLI.
- BankAccount.transactions: `ArrayList<Transaction>` maintains chronological history and efficient append.

---

## Inheritance & Polymorphism
- `BankAccount` defines common state/behavior: account number, owner, balance, status, `deposit()`, `withdraw()`, and an abstract `canWithdraw()` used polymorphically.
- `SavingsAccount` enforces minimum balance by overriding `canWithdraw()`.
- `CheckingAccount` enforces overdraft limit by overriding `canWithdraw()`.
- ATM and Bank transfer logic operate on `BankAccount` references without needing concrete type checks, relying on polymorphic `canWithdraw()`.

---

## Error Handling Strategy
- Input validation in CLI with numeric parsing loops; non-positive amounts are rejected and recorded as failed transactions (for deposits/withdrawals).
- Business logic exceptions:
  - `InsufficientFundsException` thrown by `BankAccount.withdraw()` when rules would be violated.
  - `InvalidAccountException` thrown by `Bank.transfer()` if accounts missing.
  - `AccountBlockedException` and `AuthenticationException` provided for extensibility (blocking and auth policies are implemented in `Bank.authenticate()`).
- Login security: after 3 failed PIN attempts, the customer is blocked until an admin unblocks them via AdminConsole.

---

## CLI Flows
- ATM (`src/bank/ATM.java`):
  - Login with Customer ID + PIN; blocks after 3 failures.
  - If multiple accounts, prompts for selection.
  - Operations: Check balance, Deposit, Withdraw (with rule enforcement), View transactions, Transfer (own or cross-customer), Switch account, Logout.
  - Prints simple text receipts for successful actions.

- AdminConsole (`src/bank/AdminConsole.java`):
  - Login with username/password (default: admin/password).
  - View all customers (IDs and their accounts).
  - View all accounts (type, number, owner, balance, status).
  - Create new account (Savings with min balance or Checking with overdraft limit) for existing customers.
  - Unblock a customer locked out by failed PINs.

---

## Build & Run
- Requires Java 11+ (uses `java.time`).

Compile from project root:
```
javac -d out -sourcepath src src/bank/**/*.java
```
Run:
```
java -cp out bank.Main
```

On Windows PowerShell, you can also run:
```
javac -d out -sourcepath src (Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
java -cp out bank.Main
```

---

## Extensibility Hooks
- Add more account types by subclassing `BankAccount`.
- Add persistence by replacing in-memory maps with DAO layer; `Bank` provides a clean boundary.
- Add fees/interest by overriding deposit/withdraw or adding periodic batch jobs.
- Enhance security by hashing PINs and introducing rate-limiting.

---

## Notes on Assessment Criteria
- OOP: Clear separation of concerns, strong cohesion within classes, loose coupling via `Bank` boundary and abstract `BankAccount`.
- Correctness & Robustness: Rule enforcement in `canWithdraw()`, authentication lockout, transaction logging, and CLI validation.
- Data Management: Chosen collections provide efficient lookup and iteration.
- Code Quality: Consistent naming, comments on key classes, enums for type safety.
- Problem Solving: Modularization allows easy future features (e.g., statements, interest, fees, audit exports).
