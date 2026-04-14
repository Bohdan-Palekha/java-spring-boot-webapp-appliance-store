package com.epam.rd.autocode.assessment.appliances.model;

public class TestConstants {
    public static final String CLASS_PACKAGE = "com.epam.rd.autocode.assessment.appliances.model";
    public static final String CATEGORY_TYPE = CLASS_PACKAGE + ".Category";
    public static final String POWER_TYPE_TYPE = CLASS_PACKAGE + ".PowerType";
    public static final String MANUFACTURER_TYPE = CLASS_PACKAGE + ".Manufacturer";
    public static final String APPLIANCE_TYPE = CLASS_PACKAGE + ".Appliance";
    public static final String USER_TYPE = CLASS_PACKAGE + ".User";
    public static final String CLIENT_TYPE = CLASS_PACKAGE + ".Client";
    public static final String EMPLOYEE_TYPE = CLASS_PACKAGE + ".Employee";
    public static final String ORDER_ROW_TYPE = CLASS_PACKAGE + ".OrderRow";
    public static final String ORDERS_TYPE = CLASS_PACKAGE + ".Orders";
    public static final String LONG_TYPE = "java.lang.Long";
    public static final String INT_TYPE = "java.lang.Integer";
    public static final String STRING_TYPE = "java.lang.String";
    public static final String BIG_DECIMAL_TYPE = "java.math.BigDecimal";
    public static final String BOOLEAN_TYPE = "java.lang.Boolean";
    public static final String SET_TYPE = "java.util.Set";

    public static class Category {
        public static final String ENUM_NAME = "Category";
        public static final int ENUM_COUNT_CONSTANTS = 2;
        public static final String ENUM_CONSTANT_BIG = "BIG";
        public static final String ENUM_CONSTANT_SMALL = "SMALL";
    }

    public static class PowerType {
        public static final String ENUM_NAME = "PowerType";
        public static final int ENUM_COUNT_CONSTANTS = 3;
        public static final String ENUM_CONSTANT_AC220 = "AC220";
        public static final String ENUM_CONSTANT_AC110 = "AC110";
        public static final String ENUM_CONSTANT_ACCUMULATOR = "ACCUMULATOR";
    }

    public static class Manufacturer {
        public static final String CLASS_NAME = "Manufacturer";
        public static final int CLASS_COUNT_FIELDS = 2;
        public static final String FIELD_ID = "id";
        public static final String FIELD_NAME = "name";
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 2;
    }

    public static class Appliance {
        public static final String CLASS_NAME = "Appliance";
        public static final int CLASS_COUNT_FIELDS = 10;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 10;
    }

    public static class User {
        public static final String CLASS_NAME = "User";
        public static final int CLASS_COUNT_FIELDS = 4;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 4;
    }

    public static class Client {
        public static final String CLASS_NAME = "Client";
        public static final int CLASS_COUNT_FIELDS = 1;
        public static final String FIELD_CARD = "card";
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 5;
    }

    public static class Employee {
        public static final String CLASS_NAME = "Employee";
        public static final int CLASS_COUNT_FIELDS = 1;
        public static final String FIELD_DEPARTMENT = "department";
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 5;
    }

    public static class OrderRow {
        public static final String CLASS_NAME = "OrderRow";
        public static final int CLASS_COUNT_FIELDS = 4;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 4;
    }

    public static class Orders {
        public static final String CLASS_NAME = "Orders";
        public static final int CLASS_COUNT_FIELDS = 5;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 5;
    }
}
