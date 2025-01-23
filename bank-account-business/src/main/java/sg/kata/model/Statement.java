package sg.kata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class Statement {
    private LocalDateTime date;
    private OperationType operationType;
    private BigDecimal amount;
    private BigDecimal balance;
}
