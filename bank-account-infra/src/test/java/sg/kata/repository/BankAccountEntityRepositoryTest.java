package sg.kata.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.kata.entity.BankAccountEntity;
import sg.kata.entity.StatementEntity;
import sg.kata.exception.AccountNotFoundException;
import sg.kata.exception.InsufficientBalanceException;
import sg.kata.model.BankAccount;
import sg.kata.model.Statement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.kata.model.OperationType.DEPOSIT;
import static sg.kata.model.OperationType.WITHDRAW;
import static sg.kata.service.BankAccountService.INSUFFICIENT_BALANCE_MESSAGE;
import static sg.kata.service.BankAccountService.INVALID_ACCOUNT_MESSAGE;

@ExtendWith(MockitoExtension.class)
public class BankAccountEntityRepositoryTest {

    @InjectMocks
    private BankAccountEntityRepository repository;

    @Mock
    private BankAccountEntityJpaRepository jpaRepository;

    @Test
    void shouldFindExistingAccount() {
        // GIVEN
        LocalDateTime now = now();
        StatementEntity statementEntity = new StatementEntity("123-1", now, DEPOSIT,
            BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));
        BankAccountEntity bankAccountEntity = new BankAccountEntity("123",
            BigDecimal.valueOf(1000), List.of(statementEntity));

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity));

        // WHEN
        BankAccount bankAccount = repository.findById("123");

        // THEN
        assertThat(bankAccount).isNotNull();
        assertThat(bankAccount.getAccountId()).isEqualTo("123");
        assertThat(bankAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(bankAccount.getStatements()).hasSize(1)
            .extracting(
                Statement::getDate,
                Statement::getOperationType,
                Statement::getAmount,
                Statement::getBalance)
            .containsExactly(
                tuple(now, DEPOSIT, BigDecimal.valueOf(1000), BigDecimal.valueOf(1000))
            );
    }

    @Test
    void shouldNotFindInvalidAccount() {
        // GIVEN
        String accountId = "fake-id";

        // WHEN - THEN
        Exception exception = assertThrows(AccountNotFoundException.class, () -> repository.findById(accountId));
        assertThat(exception.getMessage()).isEqualTo(INVALID_ACCOUNT_MESSAGE);
    }

    @Test
    void shouldUpdateAccountWithADeposit() {
        // GIVEN
        BigDecimal depositAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal newBalance = BigDecimal.valueOf(150);

        LocalDateTime now = now();
        StatementEntity statementEntity = new StatementEntity("123-1", now, DEPOSIT,
            initialBalance, initialBalance);
        List<StatementEntity> statementEntities = new ArrayList<>();
        statementEntities.add(statementEntity);
        BankAccountEntity bankAccountEntity = new BankAccountEntity("123",
            initialBalance, statementEntities);

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity));

        // WHEN
        repository.update("123", depositAmount, DEPOSIT);

        // THEN
        ArgumentCaptor<BankAccountEntity> captor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(jpaRepository).save(captor.capture());
        BankAccountEntity entity = captor.getValue();
        assertThat(entity.getAccountId()).isEqualTo("123");
        assertThat(entity.getBalance()).isEqualTo(newBalance);
        assertThat(entity.getStatements()).hasSize(2)
            .extracting(StatementEntity::getOperationType, StatementEntity::getAmount, StatementEntity::getBalance)
            .containsExactlyInAnyOrder(tuple(DEPOSIT, initialBalance, initialBalance), tuple(DEPOSIT, depositAmount, newBalance));
    }

    @Test
    void shouldUpdateAccountWithAWithdraw() {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(150);
        BigDecimal newBalance = BigDecimal.valueOf(100);

        LocalDateTime now = now();
        StatementEntity statementEntity = new StatementEntity("123-1", now, DEPOSIT,
            initialBalance, initialBalance);
        List<StatementEntity> statementEntities = new ArrayList<>();
        statementEntities.add(statementEntity);
        BankAccountEntity bankAccountEntity = new BankAccountEntity("123",
            initialBalance, statementEntities);

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity));

        // WHEN
        repository.update("123", withdrawAmount, WITHDRAW);

        // THEN
        ArgumentCaptor<BankAccountEntity> captor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(jpaRepository).save(captor.capture());
        BankAccountEntity entity = captor.getValue();
        assertThat(entity.getAccountId()).isEqualTo("123");
        assertThat(entity.getBalance()).isEqualTo(newBalance);
        assertThat(entity.getStatements()).hasSize(2)
            .extracting(StatementEntity::getOperationType, StatementEntity::getAmount, StatementEntity::getBalance)
            .containsExactlyInAnyOrder(tuple(DEPOSIT, initialBalance, initialBalance), tuple(WITHDRAW, withdrawAmount, newBalance));
    }

    @Test
    void shouldNotUpdateInvalidAccount() {
        // GIVEN
        String accountId = "fake-id";

        // WHEN - THEN
        Exception exception = assertThrows(AccountNotFoundException.class, () -> repository.update(accountId, BigDecimal.valueOf(100), DEPOSIT));
        assertThat(exception.getMessage()).isEqualTo(INVALID_ACCOUNT_MESSAGE);
    }

    @Test
    void shouldNotUpdateAccountWithNegativeValues() {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(100);
        BigDecimal initialBalance = BigDecimal.valueOf(50);

        BankAccountEntity bankAccountEntity = new BankAccountEntity("123",
            initialBalance, emptyList());

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity));

        // WHEN - THEN
        Exception exception = assertThrows(InsufficientBalanceException.class, () -> repository.update("123", withdrawAmount, WITHDRAW));
        assertThat(exception.getMessage()).isEqualTo(INSUFFICIENT_BALANCE_MESSAGE);
    }

    @Test
    void shouldMakeConcurrentDeposits() throws InterruptedException {
        // GIVEN
        BigDecimal depositAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal balance1 = BigDecimal.valueOf(150);
        BigDecimal balance2 = BigDecimal.valueOf(200);

        BankAccountEntity bankAccountEntity1 = new BankAccountEntity("123",
                initialBalance, new ArrayList<>());
        BankAccountEntity bankAccountEntity2 = new BankAccountEntity("123",
                balance1, new ArrayList<>());

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity1), Optional.of(bankAccountEntity2));

        // WHEN
        Runnable depositTask = () -> repository.update("123", depositAmount, DEPOSIT);

        Thread thread1 = new Thread(depositTask);
        Thread thread2 = new Thread(depositTask);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // THEN
        ArgumentCaptor<BankAccountEntity> captor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(jpaRepository, times(2)).save(captor.capture());
        List<BankAccountEntity> entities = captor.getAllValues();
        assertThat(entities).hasSize(2)
            .extracting(BankAccountEntity::getAccountId, BankAccountEntity::getBalance)
            .containsExactlyInAnyOrder(tuple("123", balance1), tuple("123", balance2));
    }

    @Test
    void shouldMakeConcurrentWithdraws() throws InterruptedException {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(100);

        BankAccountEntity bankAccountEntity1 = new BankAccountEntity("123",
                initialBalance, new ArrayList<>());
        BankAccountEntity bankAccountEntity2 = new BankAccountEntity("123",
                initialBalance.subtract(withdrawAmount), new ArrayList<>());

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity1), Optional.of(bankAccountEntity2));

        // WHEN
        Runnable withdrawTask = () -> repository.update("123", withdrawAmount, WITHDRAW);

        Thread thread1 = new Thread(withdrawTask);
        Thread thread2 = new Thread(withdrawTask);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // THEN
        ArgumentCaptor<BankAccountEntity> captor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(jpaRepository, times(2)).findById("123");
        verify(jpaRepository, times(2)).save(captor.capture());
        List<BankAccountEntity> entities = captor.getAllValues();
        assertThat(entities).hasSize(2)
            .extracting(BankAccountEntity::getAccountId, BankAccountEntity::getBalance)
            .containsExactlyInAnyOrder(tuple("123", initialBalance.subtract(withdrawAmount)), tuple("123", ZERO));
    }
}
