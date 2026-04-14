package com.epam.rd.autocode.assessment.appliances.exception;

public class LastAdminException extends AppException {
    public LastAdminException(Long id) {
        super("error.last.admin", id);
    }

    public LastAdminException(String s) {
        super("error.last.admin");
    }

    public LastAdminException() {
        super("error.last.admin");
    }
}
