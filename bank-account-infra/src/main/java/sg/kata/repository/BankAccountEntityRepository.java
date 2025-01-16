package sg.kata.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sg.kata.entity.BankAccountEntity;
import sg.kata.entity.StatementEntity;
import sg.kata.exception.AccountNotFoundException;
import sg.kata.exception.InsufficientBalanceException;
import sg.kata.exception.InvalidAmountException;
import sg.kata.model.BankAccount;
import sg.kata.model.OperationType;
import sg.kata.model.Statement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static sg.kata.model.OperationType.DEPOSIT;
import static sg.kata.service.BankAccountService.*;

@Repository
@RequiredArgsConstructor
public class BankAccountEntityRepository implements BankAccountRepository {

    private final BankAccountEntityJpaRepository jpaRepository;

    @Override
    public BankAccount findById(String accountId) {
        Optional<BankAccountEntity> entity = jpaRepository.findById(accountId);
        if (entity.isPresent()) {
            return entity.map(bankAccountEntity -> BankAccount.builder()
                .accountId(bankAccountEntity.getAccountId())
                .balance(bankAccountEntity.getBalance())
                .statements(bankAccountEntity.getStatements()
                    .stream()
                    .map(statementEntity -> Statement.builder()
                        .date(statementEntity.getDate())
                        .operationType(statementEntity.getOperationType())
                        .amount(statementEntity.getAmount())
                        .balance(statementEntity.getBalance())
                        .build()
                    )
                    .collect(Collectors.toList())
                )
                .build()
            ).get();
        } else {
            throw new AccountNotFoundException(INVALID_ACCOUNT_MESSAGE);
        }
    }

    @Override
    public void update(String accountId, BigDecimal amount, OperationType operationType) {
        if (amount.compareTo(ZERO) <= 0) {
            throw new InvalidAmountException(operationType.getDescription() + POSITIVE_AMOUNT_MESSAGE);
        }
        if (amount.scale() > 2) {
            throw new InvalidAmountException(PRECISION_EXCEEDED_MESSAGE);
        }
        Optional<BankAccountEntity> entity = jpaRepository.findById(accountId);
        if (entity.isPresent()) {
            BankAccountEntity bankAccountEntity = entity.get();

            BigDecimal newBalance = operationType.equals(DEPOSIT) ?
                bankAccountEntity.getBalance().add(amount) : bankAccountEntity.getBalance().subtract(amount);
            if (newBalance.compareTo(ZERO) < 0) {
                throw new InsufficientBalanceException(INSUFFICIENT_BALANCE_MESSAGE);
            }
            bankAccountEntity.setBalance(newBalance);

            List<StatementEntity> statementEntities = bankAccountEntity.getStatements();
            statementEntities.add(StatementEntity.builder()
                .date(now())
                .operationType(operationType)
                .amount(amount)
                .balance(newBalance)
                .build()
            );
            bankAccountEntity.setStatements(statementEntities);

            jpaRepository.save(bankAccountEntity);
        } else {
            throw new AccountNotFoundException(INVALID_ACCOUNT_MESSAGE);
        }
    }
}
