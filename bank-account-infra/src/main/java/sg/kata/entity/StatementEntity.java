package sg.kata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sg.kata.model.OperationType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.UUID;

@Entity
@Table(name = "STATEMENT")
@AllArgsConstructor
@Getter
@Builder
public class StatementEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = UUID)
    @Column(name = "STATEMENT_ID")
    private String id;

    @Column(name = "DATE")
    private LocalDateTime date;

    @Column(name = "OPERATION_TYPE")
    private OperationType operationType;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "BALANCE")
    private BigDecimal balance;

}
