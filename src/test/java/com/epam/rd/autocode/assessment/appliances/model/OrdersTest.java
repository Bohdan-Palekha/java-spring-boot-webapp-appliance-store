package com.epam.rd.autocode.assessment.appliances.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdersTest extends ReflectionTestBase {

    @BeforeEach
    void setUp() throws ClassNotFoundException {
        init(TestConstants.ORDERS_TYPE);
    }

    @Test
    void checkCountConstructors() {
        assertConstructorCount(2);
    }

    @Test
    void checkModifiersConstructors() {
        assertAllConstructorsPublic();
    }

    @Test
    void checkDefaultConstructor() {
        assertHasNoArgConstructor();
    }

    @Test
    void checkConstructorWithParameter() {
        assertHasParamConstructor(5);
    }

    @Test
    void checkCountFields() {
        assertFieldCount(5);
    }

    @Test
    void checkModifiersFields() {
        assertAllFieldsPrivate();
    }

    @ParameterizedTest
    @CsvSource({"id", "client", "employee", "orderRowSet"})
    void checkFieldsNames(String name) {
        long count = allFields.stream().filter(f -> f.getName().equals(name)).count();
        assertEquals(1, count, "Field '" + name + "' not found");
    }

    @ParameterizedTest(name = "Field {1} of type {0}")
    @CsvFileSource(resources = "/OrdersField.csv")
    void checkNameFieldType(String fieldType, String fieldName, long expected) {
        long count = allFields.stream()
                .filter(f -> f.getType().getTypeName().equals(fieldType) && f.getName().equals(fieldName))
                .count();
        assertEquals(expected, count,
                "Expected " + expected + " field '" + fieldName + "' of type '" + fieldType + "'");
    }

    @Test
    @DisplayName("approved field is Boolean (wrapper, not primitive)")
    void checkApprovedIsBoolean() {
        assertFieldExists("approved", TestConstants.BOOLEAN_TYPE);
    }

    @Test
    @DisplayName("5-param constructor has correct types including Set<OrderRow>")
    void checkParameterTypeForConstructorWithParameter() {
        Constructor<?> c = getConstructorWithParams(5);
        Parameter[] params = c.getParameters();

        // Must have Long
        assertTrue(Arrays.stream(params).anyMatch(p -> p.getType().getTypeName().equals(TestConstants.LONG_TYPE)),
                "Need Long param");
        // Must have Client
        assertTrue(Arrays.stream(params).anyMatch(p -> p.getType().getTypeName().equals(TestConstants.CLIENT_TYPE)),
                "Need Client param");
        // Must have Employee
        assertTrue(Arrays.stream(params).anyMatch(p -> p.getType().getTypeName().equals(TestConstants.EMPLOYEE_TYPE)),
                "Need Employee param");
        // Set<OrderRow> — check via parameter.toString() (preserves generic info)
        assertTrue(Arrays.stream(params).anyMatch(p ->
                        p.toString().contains("java.util.Set<com.epam.rd.autocode.assessment.appliances.model.OrderRow>")),
                "Need Set<OrderRow> param");
    }
}
