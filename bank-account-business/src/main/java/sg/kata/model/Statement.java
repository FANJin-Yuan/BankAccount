package sg.kata.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class Statement {
    LocalDateTime date;
    OperationType operationType;
    BigDecimal amount;
    BigDecimal balance;
}
