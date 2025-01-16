package sg.kata.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sg.kata.service.BankAccountService;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankAccountController.class)
public class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankAccountService service;

    @Test
    void shouldMakeADeposit() throws Exception {
        // GIVEN
        String accountId = "123";
        BigDecimal amount = new BigDecimal(100);

        // WHEN - THEN
        mockMvc.perform(
            post("/api/accounts/{accountId}/deposit", accountId)
                .param("amount", amount.toString())
        ).andExpect(status().isOk());

        verify(service).deposit(accountId, amount);
    }

    @Test
    void shouldMakeAWithdraw() throws Exception {
        // GIVEN
        String accountId = "123";
        BigDecimal amount = new BigDecimal(50);

        // WHEN -THEN
        mockMvc.perform(
            post("/api/accounts/{accountId}/withdraw", accountId)
                .param("amount", amount.toString())
        ).andExpect(status().isOk());

        verify(service).withdraw(accountId, amount);
    }

    @Test
    void shouldGetBalance() throws Exception {
        // GIVEN
        String accountId = "123";
        BigDecimal balance = new BigDecimal(1000);

        when(service.getBalance(accountId)).thenReturn(balance);

        // WHEN - THEN
        mockMvc.perform(get("/api/accounts/{accountId}/balance", accountId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("1000"));

        verify(service).getBalance(accountId);
    }

    @Test
    void shouldPrintStatement() throws Exception {
        // GIVEN
        String accountId = "123";
        String statement = "2025-01-15: DEPOSIT 100 (Balance: 100)";

        when(service.printStatement(accountId)).thenReturn(statement);

        // WHEN - THEN
        mockMvc.perform(get("/api/accounts/{accountId}/statement", accountId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(statement)));

        verify(service).printStatement(accountId);
    }
}
