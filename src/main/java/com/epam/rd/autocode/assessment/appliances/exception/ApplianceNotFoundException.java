package com.epam.rd.autocode.assessment.appliances.exception;

public class ApplianceNotFoundException extends AppException {
    public ApplianceNotFoundException(Long id) {
        super("error.appliance.notfound", id);
    }

    public ApplianceNotFoundException(String s) {
        super("error.appliance.notfound");
    }

    public ApplianceNotFoundException() {
        super("error.appliance.notfound");
    }
}
