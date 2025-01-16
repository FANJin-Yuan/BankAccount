package sg.kata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sg.kata.exception.InsufficientBalanceException;
import sg.kata.exception.InvalidAmountException;
import sg.kata.model.BankAccount;
import sg.kata.model.OperationType;
import sg.kata.model.Statement;
import sg.kata.repository.BankAccountRepository;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static sg.kata.model.OperationType.DEPOSIT;
import static sg.kata.model.OperationType.WITHDRAW;

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
    public static final String NO_STATEMENT = "Account has no statement.";


    private final BankAccountRepository bankAccountRepository;

    public synchronized void deposit(String accountId, BigDecimal amount) {
        compareWithZero(amount, DEPOSIT);
        validatePrecision(amount);
        bankAccountRepository.update(accountId, amount, DEPOSIT);
    }

    public synchronized void withdraw(String accountId, BigDecimal amount) {
        BankAccount account = bankAccountRepository.findById(accountId);
        compareWithZero(amount, WITHDRAW);
        validatePrecision(amount);
        validateBalance(amount, account.getBalance());
        bankAccountRepository.update(accountId, amount, WITHDRAW);
    }

    public BigDecimal getBalance(String accountId) {
        BankAccount account = bankAccountRepository.findById(accountId);
        return account.getBalance();
    }

    public String printStatement(String accountId) {
        BankAccount account = bankAccountRepository.findById(accountId);
        if (account.getStatements().isEmpty()) {
            return NO_STATEMENT;
        }
        List<Statement> statements = account.getStatements()
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

    private void compareWithZero(BigDecimal amount, OperationType operationType) {
        if (amount.compareTo(ZERO) <= 0) {
            throw new InvalidAmountException(operationType.getDescription() + POSITIVE_AMOUNT_MESSAGE);
        }
    }

    private void validatePrecision(BigDecimal amount) {
        if (amount.scale() > 2) {
            throw new InvalidAmountException(PRECISION_EXCEEDED_MESSAGE);
        }
    }

    private void validateBalance(BigDecimal amount, BigDecimal balance) {
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientBalanceException(INSUFFICIENT_BALANCE_MESSAGE);
        }
    }
}
