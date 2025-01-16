package sg.kata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.kata.entity.BankAccountEntity;

public interface BankAccountEntityJpaRepository extends JpaRepository<BankAccountEntity, String> {
}
