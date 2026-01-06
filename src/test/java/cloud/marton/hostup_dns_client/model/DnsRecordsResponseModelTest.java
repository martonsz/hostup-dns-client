package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DnsRecordsResponseModelTest {

    private static final Path RESPONSES_DIR = Path.of("src", "test", "resources", "hostupApiResponses");

    private static final DslJson<Object> DSL_JSON =
            new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

    @Test
    void deserialize_getDnsRecords_fixture_success() throws IOException {
        String body = Files.readString(RESPONSES_DIR.resolve("getDnsRecords.json"), StandardCharsets.UTF_8);

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        DnsRecordsResponse response = DSL_JSON.deserialize(DnsRecordsResponse.class, bodyBytes, bodyBytes.length);

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("mocked-request-id-123456", response.requestId());

        assertNotNull(response.data());
        assertNotNull(response.data().zone());
        assertEquals(10000, response.data().zone().id());
        assertEquals("marton.cloud", response.data().zone().domain());

        assertNotNull(response.data().zone().records());
        assertEquals(3, response.data().zone().records().size());

        DnsRecordsResponse.Record first = response.data().zone().records().getFirst();
        assertEquals(32857634, first.id());
        assertEquals("A", first.type());
        assertEquals("www.marton.cloud", first.name());
        assertEquals(3600, first.ttl());
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
        assertThrows(Exception.class, () -> DSL_JSON.deserialize(DnsRecordsResponse.class, bodyBytes, bodyBytes.length));
    }

    @Test
    void deserialize_missingMandatoryRecords_fails() {
        String json = """
                {
                  "success": true,
                  "requestId": "x",
                  "data": {
                    "zone": {
                      "id": "10000",
                      "domain": "marton.cloud"
                    }
                  }
                }
                """;

        byte[] bodyBytes = json.getBytes(StandardCharsets.UTF_8);
        assertThrows(Exception.class, () -> DSL_JSON.deserialize(DnsRecordsResponse.class, bodyBytes, bodyBytes.length));
    }
}
