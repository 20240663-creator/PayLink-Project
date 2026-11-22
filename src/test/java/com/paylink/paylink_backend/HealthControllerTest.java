package com.paylink.paylink_backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    @Test
    void healthEndpointShouldReturnUpMessage() {
        HealthController controller = new HealthController();
        assertEquals("PayLink is UP!", controller.health());
    }
}