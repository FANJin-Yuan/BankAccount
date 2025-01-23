package sg.kata.request;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class AccountOperationRequest {
    String accountId;
    BigDecimal amount;
}
