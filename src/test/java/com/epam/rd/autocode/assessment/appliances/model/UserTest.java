package com.epam.rd.autocode.assessment.appliances.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest extends ReflectionTestBase {

    @BeforeEach
    void setUp() throws ClassNotFoundException {
        init(TestConstants.USER_TYPE);
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

    @ParameterizedTest
    @CsvSource({"id,1", "name,1", "email,1", "password,1"})
    void checkFieldByName(String name, long expected) {
        long count = allFields.stream().filter(f -> f.getName().equals(name)).count();
        assertEquals(expected, count, "Field '" + name + "' count mismatch");
    }

    @ParameterizedTest(name = "Field {1} of type {0}")
    @CsvFileSource(resources = "/UserField.csv")
    void checkNameFieldType(String fieldType, String fieldName, long expected) {
        long count = allFields.stream()
                .filter(f -> f.getType().getTypeName().equals(fieldType) && f.getName().equals(fieldName))
                .count();
        assertEquals(expected, count);
    }

    @Test
    @DisplayName("4-param constructor: 1 Long + 3 Strings")
    void checkParameterTypeForConstructorWithParameter() {
        Constructor<?> c = getConstructorWithParams(4);
        assertEquals(1, countParamsByType(c, TestConstants.LONG_TYPE), "1 Long (id)");
        assertEquals(3, countParamsByType(c, TestConstants.STRING_TYPE), "3 Strings (name,email,password)");
    }
}
