package sg.kata.model;

import lombok.Getter;

@Getter
public enum OperationType {
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }
}
