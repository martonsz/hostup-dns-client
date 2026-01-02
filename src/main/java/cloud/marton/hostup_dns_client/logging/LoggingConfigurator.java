package cloud.marton.hostup_dns_client.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public final class LoggingConfigurator {
    private static final String CONFIG_PATH = "/logging.properties";

    private LoggingConfigurator() {
    }

    public static void configure() {
        try (InputStream input = LoggingConfigurator.class.getResourceAsStream(CONFIG_PATH)) {
            if (input == null) {
                throw new IllegalStateException("Missing logging configuration file: " + CONFIG_PATH);
            }
            LogManager.getLogManager().readConfiguration(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load logging configuration", e);
        }
    }
}
