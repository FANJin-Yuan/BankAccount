package sg.kata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.kata.model.BankAccount;
import sg.kata.model.Statement;
import sg.kata.repository.BankAccountRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static sg.kata.model.OperationType.DEPOSIT;
import static sg.kata.model.OperationType.WITHDRAW;
import static sg.kata.service.BankAccountService.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceTest {

    @InjectMocks
    private BankAccountService service;

    @Mock
    private BankAccountRepository repository;

    @Test
    void shouldMakeADeposit() {
        // GIVEN
        BigDecimal depositAmount = BigDecimal.valueOf(50);
        List<Statement> statements = new ArrayList<>();
        BankAccount bankAccount = new BankAccount("123", ZERO, statements);

        when(repository.findById("123")).thenReturn(bankAccount);

        // WHEN
        service.deposit("123", depositAmount);

        // THEN
        Statement statement = new Statement(now(), DEPOSIT, depositAmount, depositAmount);
        statements.add(statement);
        bankAccount.setStatements(statements);
        bankAccount.setBalance(ZERO.add(depositAmount));
        verify(repository).update(bankAccount);
    }

    @Test
    void shouldMakeAWithdraw() {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal newBalance = initialBalance.subtract(withdrawAmount);

        List<Statement> statements = new ArrayList<>();
        BankAccount bankAccount = new BankAccount("123", initialBalance, statements);
        when(repository.findById("123")).thenReturn(bankAccount);

        // WHEN
        service.withdraw("123", withdrawAmount);

        // THEN
        Statement statement = new Statement(now(), WITHDRAW, withdrawAmount, newBalance);
        statements.add(statement);
        bankAccount.setStatements(statements);
        bankAccount.setBalance(newBalance);
        verify(repository).update(bankAccount);
    }

    @Test
    void shouldGetBalance() {
        // GIVEN
        BankAccount account = BankAccount.builder()
            .accountId("123")
            .balance(BigDecimal.valueOf(100))
            .statements(emptyList())
            .build();

        when(repository.findById("123")).thenReturn(account);

        // WHEN
        BigDecimal balance = service.getBalance("123");

        // THEN
        assertThat(balance).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void shouldMakeConcurrentDeposits() throws InterruptedException {
        // GIVEN
        BigDecimal depositAmount = BigDecimal.valueOf(50);
        BankAccount bankAccount1 = new BankAccount("123", ZERO, new ArrayList<>());
        BankAccount bankAccount2 = new BankAccount("123", ZERO.add(depositAmount), new ArrayList<>());

        when(repository.findById("123")).thenReturn(bankAccount1, bankAccount2);

        // WHEN
        Runnable depositTask = () -> service.deposit("123", depositAmount);

        Thread thread1 = new Thread(depositTask);
        Thread thread2 = new Thread(depositTask);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // THEN
        verify(repository, times(2)).findById("123");
        bankAccount1.setBalance(ZERO.add(depositAmount));
        verify(repository).update(bankAccount1);
        bankAccount2.setBalance(depositAmount.add(depositAmount));
        verify(repository).update(bankAccount2);
    }

    @Test
    void shouldMakeConcurrentWithdraws() throws InterruptedException {
        // GIVEN
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal withdrawAmount = new BigDecimal(50);
        BankAccount bankAccount1 = new BankAccount("123", initialBalance, new ArrayList<>());
        BankAccount bankAccount2 = new BankAccount("123", initialBalance.subtract(withdrawAmount), new ArrayList<>());

        when(repository.findById("123")).thenReturn(bankAccount1, bankAccount2);

        // WHEN
        Runnable withdrawTask = () -> service.withdraw("123", withdrawAmount);

        Thread thread1 = new Thread(withdrawTask);
        Thread thread2 = new Thread(withdrawTask);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // THEN
        verify(repository, times(2)).findById("123");
        bankAccount1.setBalance(initialBalance.subtract(withdrawAmount));
        verify(repository).update(bankAccount1);
        bankAccount2.setBalance(ZERO);
        verify(repository).update(bankAccount2);
    }

    @Test
    void shouldPrintStatements() {
        // GIVEN
        Statement statement1 = Statement.builder()
            .date(now())
            .operationType(DEPOSIT)
            .amount(BigDecimal.valueOf(100))
            .balance(BigDecimal.valueOf(100))
            .build();
        Statement statement2 = Statement.builder()
            .date(now())
            .operationType(WITHDRAW)
            .amount(BigDecimal.valueOf(30))
            .balance(BigDecimal.valueOf(70))
            .build();
        BankAccount account = BankAccount.builder()
            .accountId("123")
            .balance(BigDecimal.valueOf(100))
            .statements(List.of(statement1, statement2))
            .build();

        when(repository.findById("123")).thenReturn(account);

        // WHEN
        String statement = service.printStatement("123");

        // THEN
        assertThat(statement.contains("DEPOSIT")).isTrue();
        assertThat(statement.contains("WITHDRAW")).isTrue();
        assertThat(statement.contains("100.00")).isTrue();
        assertThat(statement.contains("30.00")).isTrue();
        assertThat(statement.contains("70.00")).isTrue();
    }

    @Test
    void shouldNotPrintStatements() {
        // GIVEN
        BankAccount account = BankAccount.builder()
            .accountId("123")
            .balance(BigDecimal.valueOf(100))
            .statements(emptyList())
            .build();

        when(repository.findById("123")).thenReturn(account);

        // WHEN
        String statement = service.printStatement("123");

        // THEN
        assertThat(statement).isEqualTo(ACCOUNT_WITHOUT_STATEMENT);
    }
}
