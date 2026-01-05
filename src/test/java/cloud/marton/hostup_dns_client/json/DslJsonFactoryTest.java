package cloud.marton.hostup_dns_client.json;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to ensure that all generated DslJson converters are registered in the DslJsonFactory.
 * This helps GraalVM native image builds to include all necessary classes.
 */
class DslJsonFactoryTest {

    @Test
    void allGeneratedConvertersAreRegistered() throws IOException {
        Path generatedDir = Path.of(
                "build",
                "generated",
                "sources",
                "annotationProcessor",
                "java",
                "main",
                "cloud",
                "marton",
                "hostup_dns_client",
                "model"
        );

        try (Stream<Path> files = Files.walk(generatedDir)) {
            Set<String> converterClasses = files
                    .map(p -> p.getFileName().toString())
                    .filter(string -> string.endsWith("_DslJsonConverter.java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .collect(Collectors.toSet());

            String factorySource = Files.readString(
                    Path.of("src", "main", "java", "cloud", "marton", "hostup_dns_client", "json", "DslJsonFactory.java")
            );

            for (String converter : converterClasses) {
                String fqdn = "cloud.marton.hostup_dns_client.model." + converter;
                assertTrue(
                        factorySource.contains("new " + fqdn + "()"),
                        () -> "Missing converter registration: " + fqdn
                );
            }
        }
    }
}
