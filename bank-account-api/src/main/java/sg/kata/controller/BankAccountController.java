package sg.kata.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sg.kata.service.BankAccountService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping("/{accountId}/deposit")
    @Transactional
    public void deposit(@PathVariable @NonNull String accountId, @RequestParam @NonNull BigDecimal amount) {
        bankAccountService.deposit(accountId, amount);
    }

    @PostMapping("/{accountId}/withdraw")
    @Transactional
    public void withdraw(@PathVariable @NonNull String accountId, @RequestParam @NonNull BigDecimal amount) {
        bankAccountService.withdraw(accountId, amount);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable @NonNull String accountId) {
        BigDecimal balance = bankAccountService.getBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{accountId}/statement")
    public ResponseEntity<String> printStatement(@PathVariable @NonNull String accountId) {
        String statement = bankAccountService.printStatement(accountId);
        return ResponseEntity.ok(statement);
    }
}
