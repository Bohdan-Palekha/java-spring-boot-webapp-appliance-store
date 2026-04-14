package com.epam.rd.autocode.assessment.appliances.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTest extends ReflectionTestBase {

    @BeforeAll
    static void setup() throws ClassNotFoundException {
        init(TestConstants.CLIENT_TYPE);
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
        assertFieldCount(1);
    }

    @Test
    void checkModifiersFields() {
        assertAllFieldsPrivate();
    }

    @Test
    @DisplayName("Superclass is User")
    void checkSuperclassIsUser() {
        Class<?> superclass = allFields.get(0).getDeclaringClass().getSuperclass();
        assertEquals(TestConstants.USER_TYPE, superclass.getTypeName());
    }

    @Test
    @DisplayName("Field 'card' of type String")
    void checkCardField() {
        assertFieldExists("card", TestConstants.STRING_TYPE);
    }

    @Test
    @DisplayName("5-param constructor: 1 Long + 4 Strings")
    void checkParameterTypes() {
        Constructor<?> c = getConstructorWithParams(5);
        assertEquals(1, countParamsByType(c, TestConstants.LONG_TYPE), "1 Long");
        assertEquals(4, countParamsByType(c, TestConstants.STRING_TYPE), "4 Strings");
    }
}
