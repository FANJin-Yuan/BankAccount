package sg.kata.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sg.kata.entity.BankAccountEntity;
import sg.kata.entity.StatementEntity;
import sg.kata.exception.AccountNotFoundException;
import sg.kata.model.BankAccount;
import sg.kata.model.Statement;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public void update(BankAccount bankAccount) {
        Optional<BankAccountEntity> bankAccountEntity = jpaRepository.findById(bankAccount.getAccountId());
        if (bankAccountEntity.isPresent()) {
            BankAccountEntity entity = bankAccountEntity.get();
            List<StatementEntity> statementEntities = entity.getStatements();
            List<Statement> statements = bankAccount.getStatements();

            if (statements.isEmpty() || statements.size() - statementEntities.size() != 1) {
                throw new IllegalArgumentException(UPDATE_WITHOUT_STATEMENT);
            } else {
                Statement newStatement = statements.get(statements.size() - 1);
                StatementEntity newStatementEntity = StatementEntity.builder()
                    .date(newStatement.getDate())
                    .operationType(newStatement.getOperationType())
                    .amount(newStatement.getAmount())
                    .balance(newStatement.getBalance())
                    .build();
                statementEntities.add(newStatementEntity);

                entity.setStatements(statementEntities);
                entity.setBalance(bankAccount.getBalance());
                jpaRepository.save(entity);
            }
        } else {
            throw new AccountNotFoundException(INVALID_ACCOUNT_MESSAGE);
        }
    }
}
