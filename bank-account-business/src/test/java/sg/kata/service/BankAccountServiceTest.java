package sg.kata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceTest {

    @Test
    void shouldMakeADepositWithSuccess() {
        // GIVEN
        String accountId = "123";
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal depositAmount = BigDecimal.valueOf(50);

        BankAccount account = new BankAccount(accountId, initialBalance);

        when(repository.findById(accountId)).thenReturn(account);

        // WHEN
        service.deposit(accountId, depositAmount);

        // THEN
        assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(150));
        verify(repository).save(account);
    }

    @Test
    void shouldMakeAWithdrawWithSuccess() {
        // GIVEN
        String accountId = "123";
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal withdrawalAmount = BigDecimal.valueOf(50);

        BankAccount account = new BankAccount(accountId, initialBalance);

        when(repository.findById(accountId)).thenReturn(account);

        // WHEN
        service.withdraw(accountId, withdrawalAmount);

        // THEN
        assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(50));
        verify(repository).save(account);
    }

    @Test
    void shouldNotMakeAWithdrawIfInsufficientBalance() {
        // GIVEN
        String accountId = "123";
        BigDecimal initialBalance = BigDecimal.valueOf(30);
        BigDecimal withdrawalAmount = BigDecimal.valueOf(50);

        BankAccount account = new BankAccount(accountId, initialBalance);

        when(repository.findById(accountId)).thenReturn(account);

        // WHEN -THEN
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> service.withdraw(accountId, withdrawalAmount));
        assertThat(exception.getMessage()).isEqualTo("Insufficient balance.");
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        // GIVEN
        String accountId = "fake-id";

        when(repository.findById(accountId)).thenReturn(null);

        // WHEN - THEN
        Exception exception = assertThrows(AccountNotFoundException.class, () -> {
            service.getBalance(accountId);
        });

        assertThat(exception.getMessage()).isEqualTo("Account with ID fake-id not found.");
    }

    @Test
    void shouldMakeConcurrentDeposits() throws InterruptedException {
        // GIVEN
        String accountId = "123";
        BigDecimal initialBalance = BigDecimal.valueOf(100);

        BankAccount account = new BankAccount(accountId, initialBalance);
        when(repository.findById(accountId)).thenReturn(account);

        // WHEN
        Runnable depositTask = () -> service.deposit(accountId, BigDecimal.valueOf(10));

        Thread thread1 = new Thread(depositTask);
        Thread thread2 = new Thread(depositTask);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // THEN
        assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(120));
        verify(repository, times(2)).save(account);
    }

    @Test
    void shouldMakeConcurrentWithdrawals() throws InterruptedException {
        // Arrange
        String accountId = "123";
        BigDecimal initialBalance = new BigDecimal("1000");
        BankAccount account = new BankAccount(accountId, initialBalance);

        when(bankAccountRepository.findById(accountId)).thenReturn(account);

        int numberOfThreads = 10;
        BigDecimal withdrawalAmount = new BigDecimal("100");
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                synchronized (account) {
                    try {
                        bankAccountService.withdraw(accountId, withdrawalAmount);
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

        // Assert
        BigDecimal expectedBalance = initialBalance.subtract(withdrawalAmount.multiply(BigDecimal.valueOf(numberOfThreads)));
        assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) >= 0, "Balance should not go below zero");
        assertEquals(expectedBalance.max(BigDecimal.ZERO), account.getBalance());
        verify(bankAccountRepository, times(numberOfThreads)).findById(accountId);
    }

    @Test
    void shouldPrintStatements() {
        // GIVEN
        BankAccount account = new BankAccount("123", BigDecimal.ZERO);

        account.deposit(BigDecimal.valueOf(100));
        account.withdraw(BigDecimal.valueOf(50));

        // WHEN
        String statement = account.printStatement();

        // THEN
        assertThat(statement.contains("DEPOSIT")).isTrue();
        assertThat(statement.contains("WITHDRAWAL")).isTrue();
        assertThat(statement.contains("100.00")).isTrue();
        assertThat(statement.contains("50.00")).isTrue();
    }
}
