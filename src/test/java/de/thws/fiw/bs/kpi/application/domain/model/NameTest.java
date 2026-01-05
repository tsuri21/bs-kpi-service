package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class NameTest {

    @Test
    void init_nullValue_throwsException() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new Name(null));
        assertEquals("Name must not be null", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void init_blankValue_throwsException(String blankValue) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Name(blankValue));
        assertEquals("Name must not be blank", ex.getMessage());
    }

    @Test
    void init_validName_success() {
        assertEquals("Project", new Name("Project").value());
    }
}