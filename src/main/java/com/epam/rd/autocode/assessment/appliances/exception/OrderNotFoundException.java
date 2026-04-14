package com.epam.rd.autocode.assessment.appliances.exception;

public class OrderNotFoundException extends AppException {
    public OrderNotFoundException(Long id) {
        super("error.order.notfound", id);
    }

    public OrderNotFoundException(String s) {
        super("error.order.notfound");
    }

    public OrderNotFoundException() {
        super("error.order.notfound");
    }
}
