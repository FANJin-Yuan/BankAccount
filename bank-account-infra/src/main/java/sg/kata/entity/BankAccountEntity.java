package sg.kata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static jakarta.persistence.GenerationType.UUID;

@Entity
@Table(name = "BANK_ACCOUNT")
@AllArgsConstructor
@Getter
public class BankAccountEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = UUID)
    @Column(name = "ACCOUNT_ID")
    private String accountId;

    @Column(name = "BALANCE")
    @Setter
    private BigDecimal balance;

    @OneToMany
    @Setter
    private List<StatementEntity> statements;
}
