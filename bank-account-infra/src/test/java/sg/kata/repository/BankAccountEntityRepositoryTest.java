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
import static sg.kata.service.BankAccountService.INVALID_ACCOUNT_MESSAGE;
import static sg.kata.service.BankAccountService.UPDATE_WITHOUT_STATEMENT;

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

        List<Statement> statements = new ArrayList<>();
        statements.add(new Statement(now, DEPOSIT, initialBalance, initialBalance));
        statements.add(new Statement(now.plusMinutes(1), DEPOSIT, depositAmount, newBalance));
        BankAccount bankAccount = new BankAccount("123", newBalance, statements);

        List<StatementEntity> statementEntities = new ArrayList<>();
        statementEntities.add(new StatementEntity("123-1", now, DEPOSIT,
            initialBalance, initialBalance));
        BankAccountEntity bankAccountEntity = new BankAccountEntity("123",
            initialBalance, statementEntities);

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity));

        // WHEN
        repository.update(bankAccount);

        // THEN
        ArgumentCaptor<BankAccountEntity> captor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(jpaRepository).save(captor.capture());
        BankAccountEntity entity = captor.getValue();
        assertThat(entity.getAccountId()).isEqualTo("123");
        assertThat(entity.getBalance()).isEqualTo(newBalance);
        assertThat(entity.getStatements()).hasSize(2)
            .extracting(StatementEntity::getOperationType, StatementEntity::getAmount, StatementEntity::getBalance)
            .containsExactly(tuple(DEPOSIT, initialBalance, initialBalance), tuple(DEPOSIT, depositAmount, newBalance));
    }

    @Test
    void shouldUpdateAccountWithAWithdraw() {
        // GIVEN
        BigDecimal withdrawAmount = BigDecimal.valueOf(50);
        BigDecimal initialBalance = BigDecimal.valueOf(150);
        BigDecimal newBalance = BigDecimal.valueOf(100);
        LocalDateTime now = now();

        List<Statement> statements = new ArrayList<>();
        statements.add(new Statement(now, DEPOSIT, initialBalance, initialBalance));
        statements.add(new Statement(now.plusMinutes(1), WITHDRAW, withdrawAmount, newBalance));
        BankAccount bankAccount = new BankAccount("123", newBalance, statements);

        List<StatementEntity> statementEntities = new ArrayList<>();
        statementEntities.add(new StatementEntity("123-1", now, DEPOSIT,
            initialBalance, initialBalance));
        BankAccountEntity bankAccountEntity = new BankAccountEntity("123",
            initialBalance, statementEntities);

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity));

        // WHEN
        repository.update(bankAccount);

        // THEN
        ArgumentCaptor<BankAccountEntity> captor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(jpaRepository).save(captor.capture());
        BankAccountEntity entity = captor.getValue();
        assertThat(entity.getAccountId()).isEqualTo("123");
        assertThat(entity.getBalance()).isEqualTo(newBalance);
        assertThat(entity.getStatements()).hasSize(2)
            .extracting(StatementEntity::getOperationType, StatementEntity::getAmount, StatementEntity::getBalance)
            .containsExactly(tuple(DEPOSIT, initialBalance, initialBalance), tuple(WITHDRAW, withdrawAmount, newBalance));
    }

    @Test
    void shouldNotUpdateInvalidAccount() {
        // GIVEN
        BankAccount bankAccount = new BankAccount("fake-id", ZERO, emptyList());

        // WHEN - THEN
        Exception exception = assertThrows(AccountNotFoundException.class, () -> repository.update(bankAccount));
        assertThat(exception.getMessage()).isEqualTo(INVALID_ACCOUNT_MESSAGE);
    }

    @Test
    void shouldNotUpdateAccountWithoutNewStatement() {
        // GIVEN
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal newBalance = BigDecimal.valueOf(150);
        LocalDateTime now = now();

        List<Statement> statements = new ArrayList<>();
        statements.add(new Statement(now, DEPOSIT, initialBalance, initialBalance));
        BankAccount bankAccount = new BankAccount("123", newBalance, statements);

        List<StatementEntity> statementEntities = new ArrayList<>();
        statementEntities.add(new StatementEntity("123-1", now, DEPOSIT,
            initialBalance, initialBalance));
        BankAccountEntity bankAccountEntity = new BankAccountEntity("123",
            initialBalance, statementEntities);

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity));

        // WHEN - THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> repository.update(bankAccount));
        assertThat(exception.getMessage()).isEqualTo(UPDATE_WITHOUT_STATEMENT);
    }

    @Test
    void shouldNotUpdateAccountWithoutAnyStatement() {
        // GIVEN
        BankAccount bankAccount = new BankAccount("123", BigDecimal.valueOf(100), emptyList());

        List<StatementEntity> statementEntities = new ArrayList<>();
        statementEntities.add(new StatementEntity("123-1", now(), DEPOSIT,
            BigDecimal.valueOf(50), BigDecimal.valueOf(50)));
        BankAccountEntity bankAccountEntity = new BankAccountEntity("123",
            BigDecimal.valueOf(50), statementEntities);

        when(jpaRepository.findById("123")).thenReturn(Optional.of(bankAccountEntity));

        // WHEN - THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> repository.update(bankAccount));
        assertThat(exception.getMessage()).isEqualTo(UPDATE_WITHOUT_STATEMENT);
    }
}
