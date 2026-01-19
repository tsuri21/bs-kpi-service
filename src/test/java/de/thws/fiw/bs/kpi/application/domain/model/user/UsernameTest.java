package de.thws.fiw.bs.kpi.application.domain.model.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UsernameTest {

    @Test
    void init_nullValue_throwsException() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new Username(null));
        assertEquals("Username must not be null", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void init_blankValue_throwsException(String blankValue) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Username(blankValue));
        assertEquals("Username must not be blank", ex.getMessage());
    }

    @Test
    void init_nameWithColon_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Username("Test:"));
        assertEquals("Username must not contain colons", ex.getMessage());
    }

    @Test
    void init_validName_success() {
        assertEquals("User", new Username("User").value());
    }
}