package de.thws.fiw.bs.kpi.application.port;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PageRequestTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void init_invalidPageNumber_throwsException(int invalidPage) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new PageRequest(invalidPage, 10));
        assertEquals("Page must be greater than 0", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void init_invalidPageSize_throwsException(int invalidSize) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new PageRequest(1, invalidSize));
        assertEquals("Size must be greater than 0", ex.getMessage());
    }

    @Test
    void init_validValues_success() {
        PageRequest pageRequest = new PageRequest(1, 10);

        assertEquals(1, pageRequest.pageNumber());
        assertEquals(10, pageRequest.pageSize());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            # page, size, expectedOffset
            1,      10,   0
            2,      10,   10
            5,      20,   80
            """)
    void offset_variousPageRequests_calculatesCorrectOffset(int page, int size, int expectedOffset) {
        PageRequest request = new PageRequest(page, size);
        assertEquals(expectedOffset, request.offset());
    }
}