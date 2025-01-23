package sg.kata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sg.kata.model.BankAccount;
import sg.kata.model.Statement;
import sg.kata.repository.BankAccountRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    public static final String PRECISION_EXCEEDED_MESSAGE = "Amount must not have more than two decimal places.";
    public static final String INSUFFICIENT_BALANCE_MESSAGE = "Insufficient balance.";
    public static final String INVALID_ACCOUNT_MESSAGE = "Account does not exist.";
    public static final String POSITIVE_AMOUNT_MESSAGE = " amount must be positive.";
    public static final String STATEMENT_TITLE = "Date                | Type       | Amount  | Balance\n";
    public static final String STATEMENT_DELIMITER = "-----------------------------------------------------\n";
    public static final String STATEMENT_FORMAT = "%-20s| %-10s| %-8.2f| %-8.2f\n";
    public static final String ACCOUNT_WITHOUT_STATEMENT = "Account has no statement.";
    public static final String UPDATE_WITHOUT_STATEMENT = "Update must have a statement.";


    private final BankAccountRepository bankAccountRepository;

    public synchronized void deposit(String accountId, BigDecimal amount) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId);
        bankAccount.deposit(amount);
        bankAccountRepository.update(bankAccount);
    }

    public synchronized void withdraw(String accountId, BigDecimal amount) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId);
        bankAccount.withdraw(amount);
        bankAccountRepository.update(bankAccount);
    }

    public BigDecimal getBalance(String accountId) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId);
        return bankAccount.getBalance();
    }

    public String printStatement(String accountId) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId);
        if (bankAccount.getStatements().isEmpty()) {
            return ACCOUNT_WITHOUT_STATEMENT;
        }
        List<Statement> statements = bankAccount.getStatements()
            .stream()
            .sorted((s1, s2) -> s2.getDate().compareTo(s1.getDate()))
            .toList();
        StringBuilder sb = new StringBuilder();
        sb.append(STATEMENT_TITLE);
        sb.append(STATEMENT_DELIMITER);
        for (Statement statement : statements) {
            sb.append(String.format(STATEMENT_FORMAT,
                statement.getDate(), statement.getOperationType(),
                statement.getAmount(), statement.getBalance())
            );
        }
        return sb.toString();
    }


}
