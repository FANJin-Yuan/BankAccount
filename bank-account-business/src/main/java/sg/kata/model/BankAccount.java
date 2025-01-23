package sg.kata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import sg.kata.exception.InsufficientBalanceException;
import sg.kata.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static sg.kata.model.OperationType.DEPOSIT;
import static sg.kata.model.OperationType.WITHDRAW;
import static sg.kata.service.BankAccountService.*;

@Data
@AllArgsConstructor
@Builder
public class BankAccount {
    private String accountId;
    private BigDecimal balance;
    private List<Statement> statements;

    public void deposit(BigDecimal amount) {
        compareWithZero(amount, DEPOSIT);
        validatePrecision(amount);
        balance = balance.add(amount);
        statements.add(new Statement(now(), DEPOSIT, amount, balance));
    }

    public void withdraw(BigDecimal amount) {
        compareWithZero(amount, WITHDRAW);
        validatePrecision(amount);
        validateBalance(amount, balance);
        balance = balance.subtract(amount);
        statements.add(new Statement(now(), WITHDRAW, amount, balance));
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
