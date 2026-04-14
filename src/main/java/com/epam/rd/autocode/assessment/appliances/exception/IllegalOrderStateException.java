package com.epam.rd.autocode.assessment.appliances.exception;

public class IllegalOrderStateException extends AppException {
    public IllegalOrderStateException(Long id) {
        super("error.order.invalid.state", id);
    }

    public IllegalOrderStateException(String s) {
        super("error.order.invalid.state");
    }

    public IllegalOrderStateException() {
        super("error.order.invalid.state");
    }
}
