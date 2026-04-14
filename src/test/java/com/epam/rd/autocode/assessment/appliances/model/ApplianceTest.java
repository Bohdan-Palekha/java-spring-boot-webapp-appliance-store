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

class ApplianceTest extends ReflectionTestBase {

    @BeforeEach
    void setUp() throws ClassNotFoundException {
        init(TestConstants.APPLIANCE_TYPE);
    }

    @Test
    @DisplayName("Count constructors == 2")
    void checkCountConstructors() {
        assertConstructorCount(2);
    }

    @Test
    @DisplayName("All constructors public")
    void checkModifiersConstructors() {
        assertAllConstructorsPublic();
    }

    @Test
    @DisplayName("Has no-arg constructor")
    void checkDefaultConstructor() {
        assertHasNoArgConstructor();
    }

    @Test
    @DisplayName("Has 10-param constructor")
    void checkConstructorWithParameter() {
        assertHasParamConstructor(10);
    }

    @Test
    @DisplayName("Field count == 10")
    void checkCountFields() {
        assertFieldCount(10);
    }

    @Test
    @DisplayName("All fields private")
    void checkModifiersFields() {
        assertAllFieldsPrivate();
    }

    @ParameterizedTest(name = "Field {1} of type {0}")
    @CsvFileSource(resources = "/ApplianceFields.csv")
    void checkNameFieldType(String fieldType, String fieldName, long expected) {
        long count = allFields.stream()
                .filter(f -> f.getType().getTypeName().equals(fieldType) && f.getName().equals(fieldName))
                .count();
        assertEquals(expected, count,
                "Expected " + expected + " field(s) named '" + fieldName + "' of type '" + fieldType + "'");
    }

    @Test
    @DisplayName("10-param constructor has correct param types")
    void checkParameterTypeForConstructorWithParameter() {
        Constructor<?> c = getConstructorWithParams(10);
        Parameter[] params = c.getParameters();

        // Count by type
        long longCount = Arrays.stream(params).filter(p -> p.getType().getTypeName().equals(TestConstants.LONG_TYPE)).count();
        long stringCount = Arrays.stream(params).filter(p -> p.getType().getTypeName().equals(TestConstants.STRING_TYPE)).count();
        long intCount = Arrays.stream(params).filter(p -> p.getType().getTypeName().equals(TestConstants.INT_TYPE)).count();
        long bdCount = Arrays.stream(params).filter(p -> p.getType().getTypeName().equals(TestConstants.BIG_DECIMAL_TYPE)).count();
        long categoryCount = Arrays.stream(params).filter(p -> p.getType().getTypeName().equals(TestConstants.CLASS_PACKAGE + ".Category")).count();
        long mfrCount = Arrays.stream(params).filter(p -> p.getType().getTypeName().equals(TestConstants.MANUFACTURER_TYPE)).count();
        long ptCount = Arrays.stream(params).filter(p -> p.getType().getTypeName().equals(TestConstants.CLASS_PACKAGE + ".PowerType")).count();

        assertEquals(1, longCount, "1 Long (id)");
        assertEquals(4, stringCount, "4 Strings (name,model,characteristic,description)");
        assertEquals(1, intCount, "1 Integer (power)");
        assertEquals(1, bdCount, "1 BigDecimal (price)");
        assertEquals(1, categoryCount, "1 Category");
        assertEquals(1, mfrCount, "1 Manufacturer");
        assertEquals(1, ptCount, "1 PowerType");
    }
}
