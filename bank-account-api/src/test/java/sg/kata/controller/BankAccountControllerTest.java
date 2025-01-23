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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sg.kata.controller.BankAccountController.DEPOSIT_SUCCESSFUL;
import static sg.kata.controller.BankAccountController.WITHDRAW_SUCCESSFUL;

@WebMvcTest(BankAccountController.class)
public class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankAccountService service;

    @Test
    void shouldMakeADeposit() throws Exception {
        // GIVEN
        String requestBody = "{\"accountId\": \"123\", \"amount\": 100}";

        // WHEN - THEN
        mockMvc.perform(
            post("/api/accounts/deposit")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(DEPOSIT_SUCCESSFUL)));

        verify(service).deposit("123", BigDecimal.valueOf(100));
    }

    @Test
    void shouldMakeAWithdraw() throws Exception {
        // GIVEN
        String requestBody = "{\"accountId\": \"123\", \"amount\": 50}";

        // WHEN -THEN
        mockMvc.perform(
            post("/api/accounts/withdraw")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(WITHDRAW_SUCCESSFUL)));

        verify(service).withdraw("123", BigDecimal.valueOf(50));
    }

    @Test
    void shouldGetBalance() throws Exception {
        // GIVEN
        String requestBody = "{\"accountId\": \"123\"}";

        when(service.getBalance("123")).thenReturn(BigDecimal.valueOf(100));

        // WHEN - THEN
        mockMvc.perform(
            get("/api/accounts/balance")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("100"));

        verify(service).getBalance("123");
    }

    @Test
    void shouldPrintStatement() throws Exception {
        // GIVEN
        String requestBody = "{\"accountId\": \"123\"}";
        String statement = "2025-01-15: DEPOSIT 100 (Balance: 100)";

        when(service.printStatement("123")).thenReturn(statement);

        // WHEN - THEN
        mockMvc.perform(
            get("/api/accounts/statement")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(statement)));

        verify(service).printStatement("123");
    }
}
