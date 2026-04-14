package com.epam.rd.autocode.assessment.appliances.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManufacturerTest extends ReflectionTestBase {

    @BeforeAll
    static void setup() throws ClassNotFoundException {
        init(TestConstants.MANUFACTURER_TYPE);
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
    @DisplayName("Has 2-param constructor")
    void checkConstructorWithParameter() {
        assertHasParamConstructor(2);
    }

    @Test
    @DisplayName("2-param constructor has Long and String params")
    void checkParameterTypes() {
        Constructor<?> c = getConstructorWithParams(2);
        assertEquals(1, countParamsByType(c, TestConstants.LONG_TYPE), "Need 1 Long param");
        assertEquals(1, countParamsByType(c, TestConstants.STRING_TYPE), "Need 1 String param");
    }

    @Test
    @DisplayName("Field count == 2")
    void checkCountFields() {
        assertFieldCount(2);
    }

    @Test
    @DisplayName("All fields private")
    void checkModifiersFields() {
        assertAllFieldsPrivate();
    }

    @Test
    @DisplayName("Field 'id' of type Long exists")
    void checkIdFieldType() {
        assertFieldExists("id", TestConstants.LONG_TYPE);
    }

    @Test
    @DisplayName("Field 'name' of type String exists")
    void checkNameFieldType() {
        assertFieldExists("name", TestConstants.STRING_TYPE);
    }
}
