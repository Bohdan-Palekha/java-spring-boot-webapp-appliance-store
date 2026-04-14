package com.epam.rd.autocode.assessment.appliances.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerTypeTest {
    private static List<?> constants;
    private static Class<?> clazz;

    @BeforeAll
    static void setup() throws ClassNotFoundException {
        clazz = Class.forName(TestConstants.POWER_TYPE_TYPE);
        constants = Arrays.asList(clazz.getEnumConstants());
    }

    @Test
    void checkIsEnum() {
        assertTrue(clazz.isEnum());
    }

    @Test
    void checkCountConstants() {
        assertEquals(3, constants.size());
    }

    @Test
    void checkAC220() {
        assertEquals(1, Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).filter(n -> n.equals("AC220")).count());
    }

    @Test
    void checkAC110() {
        assertEquals(1, Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).filter(n -> n.equals("AC110")).count());
    }

    @Test
    void checkACCUMULATOR() {
        assertEquals(1, Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).filter(n -> n.equals("ACCUMULATOR")).count());
    }
}
