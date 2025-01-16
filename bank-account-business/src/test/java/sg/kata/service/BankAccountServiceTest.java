package sg.kata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.kata.exception.InsufficientBalanceException;
import sg.kata.exception.InvalidAmountException;
import sg.kata.model.BankAccount;
import sg.kata.model.Statement;
import sg.kata.repository.BankAccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void shouldMakeADepositWithSuccess() {
        // GIVEN
        BigDecimal depositAmount = BigDecimal.valueOf(50);

        // WHEN
        service.deposit("123", depositAmount);

        // THEN
        verify(repository).update("123", depositAmount, DEPOSIT);
    }

    @Test
    void shouldNotMakeADepositIfAmountLessThanZero() {
        // WHEN -THEN
        Exception exception = assertThrows(InvalidAmountException.class,
            () -> service.deposit("123", BigDecimal.valueOf(-50)));
        assertThat(exception.getMessage()).isEqualTo(DEPOSIT.getDescription() + POSITIVE_AMOUNT_MESSAGE);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldNotMakeADepositWithInvalidPrecision() {
        // WHEN - THEN
        Exception exception = assertThrows(InvalidAmountException.class, () ->
            service.deposit("123", new BigDecimal("50.123")));

        assertThat(exception.getMessage()).isEqualTo(PRECISION_EXCEEDED_MESSAGE);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldMakeAWithdrawWithSuccess() {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(100);

        BankAccount account = BankAccount.builder()
            .accountId("123")
            .balance(initialBalance)
            .statements(emptyList())
            .build();
        when(repository.findById("123")).thenReturn(account);

        // WHEN
        service.withdraw("123", withdrawAmount);

        // THEN
        verify(repository).update("123", withdrawAmount, WITHDRAW);
    }

    @Test
    void shouldNotMakeAWithdrawIfAmountLessThanZero() {
        // WHEN -THEN
        Exception exception = assertThrows(InvalidAmountException.class,
            () -> service.withdraw("123", BigDecimal.valueOf(-50)));
        assertThat(exception.getMessage()).isEqualTo(WITHDRAW.getDescription() + POSITIVE_AMOUNT_MESSAGE);
        verify(repository).findById("123");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldNotMakeAWithdrawWithInvalidPrecision() {
        // WHEN - THEN
        Exception exception = assertThrows(InvalidAmountException.class, () ->
            service.withdraw("123", new BigDecimal("50.123")));

        assertThat(exception.getMessage()).isEqualTo(PRECISION_EXCEEDED_MESSAGE);
        verify(repository).findById("123");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldNotMakeAWithdrawIfInsufficientBalance() {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(30);

        BankAccount account = BankAccount.builder()
            .accountId("123")
            .balance(initialBalance)
            .statements(emptyList())
            .build();

        when(repository.findById("123")).thenReturn(account);

        // WHEN -THEN
        Exception exception = assertThrows(InsufficientBalanceException.class,
            () -> service.withdraw("123", withdrawAmount));
        assertThat(exception.getMessage()).isEqualTo(INSUFFICIENT_BALANCE_MESSAGE);
        verify(repository).findById("123");
        verifyNoMoreInteractions(repository);
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

        // WHEN
        Runnable depositTask = () -> service.deposit("123", depositAmount);

        Thread thread1 = new Thread(depositTask);
        Thread thread2 = new Thread(depositTask);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // THEN
        verify(repository, times(2)).update("123", depositAmount, DEPOSIT);
    }

    @Test
    void shouldMakeConcurrentWithdraws() throws InterruptedException {
        // GIVEN
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BankAccount account = BankAccount.builder()
            .accountId("123")
            .balance(initialBalance)
            .statements(emptyList())
            .build();
        when(repository.findById("123")).thenReturn(account);

        BigDecimal withdrawAmount = new BigDecimal(10);

        int numberOfThreads = 10;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // WHEN
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                synchronized (account) {
                    try {
                        service.withdraw("123", withdrawAmount);
                    } catch (Exception ignored) {
                        // Ignoring exceptions for this test
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // THEN
        verify(repository, times(numberOfThreads)).findById("123");
        verify(repository, times(numberOfThreads)).update("123", withdrawAmount, WITHDRAW);
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
        assertThat(statement).isEqualTo(NO_STATEMENT);
    }
}
