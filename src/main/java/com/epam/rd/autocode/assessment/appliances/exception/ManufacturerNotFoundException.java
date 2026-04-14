package com.epam.rd.autocode.assessment.appliances.exception;

public class ManufacturerNotFoundException extends AppException {
    public ManufacturerNotFoundException(Long id) {
        super("error.manufacturer.notfound", id);
    }

    public ManufacturerNotFoundException(String s) {
        super("error.manufacturer.notfound");
    }

    public ManufacturerNotFoundException() {
        super("error.manufacturer.notfound");
    }
}
