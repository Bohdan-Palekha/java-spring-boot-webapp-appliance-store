package com.epam.rd.autocode.assessment.appliances.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRowTest extends ReflectionTestBase {

    @BeforeEach
    void setUp() throws ClassNotFoundException {
        init(TestConstants.ORDER_ROW_TYPE);
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
        assertHasParamConstructor(4);
    }

    @Test
    void checkCountFields() {
        assertFieldCount(4);
    }

    @Test
    void checkModifiersFields() {
        assertAllFieldsPrivate();
    }

    @ParameterizedTest(name = "Field {1} of type {0}")
    @CsvFileSource(resources = "/OrderRowField.csv")
    void checkNameFieldType(String fieldType, String fieldName) {
        long count = allFields.stream()
                .filter(f -> f.getType().getTypeName().equals(fieldType) && f.getName().equals(fieldName))
                .count();
        assertTrue(count > 0, "Field '" + fieldName + "' of type '" + fieldType + "' not found");
    }

    @Test
    @DisplayName("4-param constructor has Appliance, BigDecimal, and 2 Longs")
    void checkParameterTypes() {
        Constructor<?> c = getConstructorWithParams(4);
        Parameter[] params = c.getParameters();

        long longCount = Arrays.stream(params).filter(p -> p.getType().getTypeName().equals(TestConstants.LONG_TYPE)).count();
        assertEquals(2, longCount, "Need 2 Long params (id + number)");

        assertTrue(Arrays.stream(params).anyMatch(p -> p.getType().getTypeName().equals(TestConstants.APPLIANCE_TYPE)),
                "Need Appliance param");
        assertTrue(Arrays.stream(params).anyMatch(p -> p.getType().getTypeName().equals(TestConstants.BIG_DECIMAL_TYPE)),
                "Need BigDecimal param");
    }
}
