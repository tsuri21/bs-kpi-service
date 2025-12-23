package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameTest {

    @Test
    void rejectsNullValue() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new Name(null));

        assertEquals("Name must not be null", ex.getMessage());
    }

    @Test
    void rejectsBlankValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Name("   "));

        assertEquals("Name must not be blank", ex.getMessage());
    }

    @Test
    void rejectsEmptyValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Name(""));

        assertEquals("Name must not be blank", ex.getMessage());
    }

    @Test
    void createsNameForValidValue() {
        Name name = new Name("Project");

        assertEquals("Project", name.value());
    }
}