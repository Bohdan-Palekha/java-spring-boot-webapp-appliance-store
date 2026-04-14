package com.epam.rd.autocode.assessment.appliances.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ReflectionTestBase {
    protected static List<Field> allFields;
    protected static List<Constructor<?>> allConstructors;

    protected static void init(String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        allFields = Arrays.asList(clazz.getDeclaredFields());
        allConstructors = Arrays.asList(clazz.getConstructors());
    }

    protected static void assertFieldCount(int expected) {
        assertEquals(expected, allFields.size(),
                "Expected " + expected + " fields but found: " +
                        allFields.stream().map(f -> f.getName() + ":" + f.getType().getSimpleName()).collect(Collectors.joining(", ")));
    }

    protected static void assertConstructorCount(int expected) {
        assertEquals(expected, allConstructors.size());
    }

    protected static void assertAllFieldsPrivate() {
        long privateCount = allFields.stream().filter(f -> Modifier.isPrivate(f.getModifiers())).count();
        assertEquals(allFields.size(), privateCount, "All fields must be private");
    }

    protected static void assertAllConstructorsPublic() {
        assertTrue(allConstructors.stream().allMatch(c -> Modifier.isPublic(c.getModifiers())));
    }

    protected static void assertHasNoArgConstructor() {
        long count = allConstructors.stream().filter(c -> c.getParameterCount() == 0).count();
        assertEquals(1, count, "Must have exactly one no-arg constructor");
    }

    protected static void assertHasParamConstructor(int paramCount) {
        long count = allConstructors.stream().filter(c -> c.getParameterCount() == paramCount).count();
        assertEquals(1, count, "Must have exactly one " + paramCount + "-param constructor");
    }

    protected static void assertFieldExists(String name, String typeName) {
        long count = allFields.stream()
                .filter(f -> f.getName().equals(name) && f.getType().getTypeName().equals(typeName))
                .count();
        assertEquals(1, count, "Field '" + name + "' of type '" + typeName + "' not found");
    }

    protected static Constructor<?> getConstructorWithParams(int paramCount) {
        return allConstructors.stream()
                .filter(c -> c.getParameterCount() == paramCount)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No " + paramCount + "-param constructor found"));
    }

    protected static long countParamsByType(Constructor<?> c, String typeName) {
        return Arrays.stream(c.getParameters())
                .filter(p -> p.getType().getTypeName().equals(typeName))
                .count();
    }
}
