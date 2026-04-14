package com.epam.rd.autocode.assessment.appliances.exception;

public class ManufacturerHasAppliancesException extends AppException {
    public ManufacturerHasAppliancesException(Long id) {
        super("error.manufacturer.has.appliances", id);
    }

    public ManufacturerHasAppliancesException(Long id, String name) {
        super("error.manufacturer.has.appliances", id);
    }

    public ManufacturerHasAppliancesException(String s) {
        super("error.manufacturer.has.appliances");
    }

    public ManufacturerHasAppliancesException() {
        super("error.manufacturer.has.appliances");
    }
}
