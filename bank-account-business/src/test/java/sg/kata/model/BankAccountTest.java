package sg.kata.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.kata.exception.InsufficientBalanceException;
import sg.kata.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.kata.model.OperationType.DEPOSIT;
import static sg.kata.model.OperationType.WITHDRAW;
import static sg.kata.service.BankAccountService.*;

public class BankAccountTest {

    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        bankAccount = new BankAccount("123", BigDecimal.valueOf(100), new ArrayList<>());
    }

    @Test
    void shouldMakeADepositWithSuccess() {
        // GIVEN
        BigDecimal depositAmount = BigDecimal.valueOf(50);

        // WHEN
        bankAccount.deposit(depositAmount);

        // THEN
        assertThat(bankAccount.getBalance()).isEqualTo(BigDecimal.valueOf(150));
        assertThat(bankAccount.getStatements()).hasSize(1)
            .extracting(Statement::getOperationType, Statement::getAmount, Statement::getBalance)
            .containsExactly(tuple(DEPOSIT, depositAmount, BigDecimal.valueOf(150)));
    }

    @Test
    void shouldNotMakeADepositIfAmountLessThanZero() {
        // WHEN -THEN
        Exception exception = assertThrows(InvalidAmountException.class,
            () -> bankAccount.deposit(BigDecimal.valueOf(-50)));
        assertThat(exception.getMessage()).isEqualTo(DEPOSIT.getDescription() + POSITIVE_AMOUNT_MESSAGE);
    }

    @Test
    void shouldNotMakeADepositWithInvalidPrecision() {
        // WHEN - THEN
        Exception exception = assertThrows(InvalidAmountException.class, () ->
            bankAccount.deposit(new BigDecimal("50.123")));
        assertThat(exception.getMessage()).isEqualTo(PRECISION_EXCEEDED_MESSAGE);
    }

    @Test
    void shouldMakeAWithdrawWithSuccess() {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(50);

        // WHEN
        bankAccount.withdraw(withdrawAmount);

        // THEN
        assertThat(bankAccount.getBalance()).isEqualTo(BigDecimal.valueOf(50));
        assertThat(bankAccount.getStatements()).hasSize(1)
            .extracting(Statement::getOperationType, Statement::getAmount, Statement::getBalance)
            .containsExactly(tuple(WITHDRAW, withdrawAmount, BigDecimal.valueOf(50)));
    }

    @Test
    void shouldNotMakeAWithdrawIfAmountLessThanZero() {
        // WHEN -THEN
        Exception exception = assertThrows(InvalidAmountException.class,
            () -> bankAccount.withdraw(BigDecimal.valueOf(-50)));
        assertThat(exception.getMessage()).isEqualTo(WITHDRAW.getDescription() + POSITIVE_AMOUNT_MESSAGE);
    }

    @Test
    void shouldNotMakeAWithdrawWithInvalidPrecision() {
        // WHEN - THEN
        Exception exception = assertThrows(InvalidAmountException.class, () ->
            bankAccount.withdraw(new BigDecimal("50.123")));
        assertThat(exception.getMessage()).isEqualTo(PRECISION_EXCEEDED_MESSAGE);
    }

    @Test
    void shouldNotMakeAWithdrawIfInsufficientBalance() {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(30);

        bankAccount.setBalance(initialBalance);

        // WHEN -THEN
        Exception exception = assertThrows(InsufficientBalanceException.class,
            () -> bankAccount.withdraw(withdrawAmount));
        assertThat(exception.getMessage()).isEqualTo(INSUFFICIENT_BALANCE_MESSAGE);
    }

    @Test
    void shouldGetInitialBalance() {
        // WHEN - THEN
        assertThat(bankAccount.getBalance()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void shouldGetStatements() {
        // GIVEN
        assertThat(bankAccount.getStatements()).isEmpty();

        // WHEN
        bankAccount.deposit(BigDecimal.valueOf(100));
        bankAccount.withdraw(BigDecimal.valueOf(50));

        // THEN
        assertThat(bankAccount.getStatements()).hasSize(2);
    }
}
