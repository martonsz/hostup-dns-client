package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ZonesResponseModelTest {

    private static final Path RESPONSES_DIR = Path.of("src", "test", "resources", "hostupApiResponses");

    private static final DslJson<Object> DSL_JSON =
            new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

    @Test
    void deserialize_listZones_fixture_success() throws IOException {
        String body = Files.readString(RESPONSES_DIR.resolve("listZones.json"), StandardCharsets.UTF_8);

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        ZonesResponse response = DSL_JSON.deserialize(ZonesResponse.class, bodyBytes, bodyBytes.length);

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("mocked-request-id-654321", response.requestId());

        assertNotNull(response.data());
        assertNotNull(response.data().zones());
        assertEquals(2, response.data().zones().size());

        ZonesResponse.Zone first = response.data().zones().getFirst();
        assertEquals("20", first.server_id());
        assertEquals("1234", first.account_id());
        assertEquals("10000", first.domain_id());
        assertEquals("marton.cloud", first.domain());
    }

    @Test
    void deserialize_missingMandatoryData_fails() {
        String json = """
                {
                  "success": true,
                  "requestId": "x"
                }
                """;

        byte[] bodyBytes = json.getBytes(StandardCharsets.UTF_8);
        assertThrows(Exception.class, () -> DSL_JSON.deserialize(ZonesResponse.class, bodyBytes, bodyBytes.length));
    }

    @Test
    void deserialize_missingMandatoryZones_fails() {
        String json = """
                {
                  "success": true,
                  "requestId": "x",
                  "data": { }
                }
                """;

        byte[] bodyBytes = json.getBytes(StandardCharsets.UTF_8);
        assertThrows(Exception.class, () -> DSL_JSON.deserialize(ZonesResponse.class, bodyBytes, bodyBytes.length));
    }
}
