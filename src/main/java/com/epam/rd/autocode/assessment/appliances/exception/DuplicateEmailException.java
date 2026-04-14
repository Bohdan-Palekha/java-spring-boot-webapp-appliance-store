package com.epam.rd.autocode.assessment.appliances.exception;

public class DuplicateEmailException extends AppException {
    public DuplicateEmailException(Long id) {
        super("user.email.taken", id);
    }

    public DuplicateEmailException(String s) {
        super("user.email.taken");
    }

    public DuplicateEmailException() {
        super("user.email.taken");
    }
}
