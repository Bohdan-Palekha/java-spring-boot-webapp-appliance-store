package com.epam.rd.autocode.assessment.appliances.exception;

public class ManufacturerNameTakenException extends AppException {
    public ManufacturerNameTakenException(String name) {
        super("error.manufacturer.name.taken");
    }

    public ManufacturerNameTakenException(Long id) {
        super("error.manufacturer.name.taken", id);
    }

    public ManufacturerNameTakenException() {
        super("error.manufacturer.name.taken");
    }
}
