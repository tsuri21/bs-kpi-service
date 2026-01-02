package de.thws.fiw.bs.kpi.application.port;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageTest {

    @Test
    void init_negativeTotal_throwsException() {
        PageRequest request = new PageRequest(1, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Page<>(List.of(), request, -1));
        assertEquals("Total must not be negative", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            # size, total, expectedPages
            10,     0,     0
            10,     10,    1
            10,     11,    2
            1,      1,     1
            7,      100,   15
            """)
    void getTotalPages_variousTotalElementsAndSizes_calculatesCorrectValue(int size, long total, long expectedPages) {
        PageRequest request = new PageRequest(1, size);
        Page<Object> page = new Page<>(List.of(), request, total);

        assertEquals(expectedPages, page.getTotalPages());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            # pageNum, total, expected
            1,         11,    true
            2,         20,    false
            1,         10,    false
            1,         5,     false
            """)
    void hasNext_variousPageStates_returnsExpectedBoolean(int pageNum, long total, boolean expected) {
        PageRequest request = new PageRequest(pageNum, 10);
        Page<Object> page = new Page<>(List.of(), request, total);

        assertEquals(expected, page.hasNext());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            # pageNum, expected
            1,         false
            2,         true
            5,         true
            """)
    void hasPrevious_variousPageNumbers_returnsExpectedBoolean(int pageNum, boolean expected) {
        PageRequest request = new PageRequest(pageNum, 10);
        Page<Object> page = new Page<>(List.of(), request, 100);

        assertEquals(expected, page.hasPrevious());
    }
}