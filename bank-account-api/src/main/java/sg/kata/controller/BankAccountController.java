package sg.kata.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sg.kata.request.AccountOperationRequest;
import sg.kata.service.BankAccountService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    public static final String DEPOSIT_SUCCESSFUL = "Deposit successful";
    public static final String WITHDRAW_SUCCESSFUL = "Withdraw successful";


    private final BankAccountService bankAccountService;

    @PostMapping("/deposit")
    @Transactional
    public ResponseEntity<String> deposit(@RequestBody @NonNull AccountOperationRequest request) {
        bankAccountService.deposit(request.getAccountId(), request.getAmount());
        return ResponseEntity.ok(DEPOSIT_SUCCESSFUL);
    }

    @PostMapping("/withdraw")
    @Transactional
    public ResponseEntity<String> withdraw(@RequestBody @NonNull AccountOperationRequest request) {
        bankAccountService.withdraw(request.getAccountId(), request.getAmount());
        return ResponseEntity.ok(WITHDRAW_SUCCESSFUL);
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestBody @NonNull AccountOperationRequest request) {
        BigDecimal balance = bankAccountService.getBalance(request.getAccountId());
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/statement")
    public ResponseEntity<String> printStatement(@RequestBody @NonNull AccountOperationRequest request) {
        String statement = bankAccountService.printStatement(request.getAccountId());
        return ResponseEntity.ok(statement);
    }
}
