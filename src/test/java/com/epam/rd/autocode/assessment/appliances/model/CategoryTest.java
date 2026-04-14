package com.epam.rd.autocode.assessment.appliances.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryTest {
    private static List<?> constants;
    private static Class<?> clazz;

    @BeforeAll
    static void setup() throws ClassNotFoundException {
        clazz = Class.forName(TestConstants.CATEGORY_TYPE);
        constants = Arrays.asList(clazz.getEnumConstants());
    }

    @Test
    @DisplayName("Category is Enum")
    void checkIsEnum() {
        assertTrue(clazz.isEnum());
    }

    @Test
    @DisplayName("2 constants")
    void checkCountConstants() {
        assertEquals(TestConstants.Category.ENUM_COUNT_CONSTANTS, constants.size());
    }

    @Test
    @DisplayName("has BIG")
    void checkBIG() {
        long c = Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).filter(n -> n.equals("BIG")).count();
        assertEquals(1, c);
    }

    @Test
    @DisplayName("has SMALL")
    void checkSMALL() {
        long c = Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).filter(n -> n.equals("SMALL")).count();
        assertEquals(1, c);
    }
}
