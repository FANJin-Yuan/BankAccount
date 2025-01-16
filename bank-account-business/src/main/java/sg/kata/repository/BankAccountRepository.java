package sg.kata.repository;

import sg.kata.model.BankAccount;
import sg.kata.model.OperationType;

import java.math.BigDecimal;

public interface BankAccountRepository {
    BankAccount findById(String accountId);
    void update(String accountId, BigDecimal amount, OperationType operationType);
}
