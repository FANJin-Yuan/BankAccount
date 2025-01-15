package sg.kata.repository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class BankAccountRepositoryTest {

    @Test
    void shouldFindExistingAccount() {
        // GIVEN
        String accountId = "123";

        // WHEN
        BankAccount account = repository.findById(accountId);

        // THEN
        assertNotNull(account);
        assertEquals(accountId, account.getAccountId());
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    @Test
    void shouldNotFindNonexistentAccount() {
        // GIVEN
        String accountId = "999";

        // WHEN - THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> repository.findById(accountId));
        assertEquals("Account not found.", exception.getMessage());
    }
}
