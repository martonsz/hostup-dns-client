package cloud.marton.hostup_dns_client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @Test
    void helpFlagRunsWithoutError() {
        assertDoesNotThrow(() -> Main.main(new String[]{"--help"}));
    }

    @Test
    void versionFlagRunsWithoutError() {
        assertDoesNotThrow(() -> Main.main(new String[]{"--version"}));
    }
}
