package com.epam.rd.autocode.assessment.appliances.exception;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(Long id) {
        super("error.user.notfound", id);
    }

    public UserNotFoundException(String s) {
        super("error.user.notfound");
    }

    public UserNotFoundException() {
        super("error.user.notfound");
    }
}
