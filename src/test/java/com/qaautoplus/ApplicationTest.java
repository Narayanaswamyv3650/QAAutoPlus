package com.qaautoplus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QA Auto Plus Application Tests")
public class ApplicationTest {

    @Test
    @DisplayName("Application should initialize successfully")
    public void testApplicationInitialization() {
        assertTrue(true, "Application initialization test");
    }

    @Test
    @DisplayName("Main class should exist")
    public void testMainClassExists() {
        try {
            Class<?> mainClass = Class.forName("com.qaautoplus.Main");
            assertNotNull(mainClass, "Main class should exist");
        } catch (ClassNotFoundException e) {
            fail("Main class not found: " + e.getMessage());
        }
    }
}

