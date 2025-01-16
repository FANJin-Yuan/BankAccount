package sg.kata.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class BankAccount {
    String accountId;
    BigDecimal balance;
    List<Statement> statements;
}
