package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.HttpErrorException;
import cloud.marton.hostup_dns_client.exceptions.JsonMappingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class HostupApiClientFailTest {

    private static final Path RESPONSES_DIR = Path.of("src", "test", "resources", "hostupApiResponses");
    private static WireMockServer wireMockServer;
    private static HostupApiClient client;

    @BeforeAll
    static void setup() throws IOException, URISyntaxException {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        stubGetZonesFailDeserialization();
        stubGetDnsRecordsWithNull();
        stubGetDnsRecordsNot200();

        URI baseUri = new URI(wireMockServer.baseUrl() + "/");
        client = new HostupApiClient("test-api-key", baseUri);
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void getZones() {
        assertThrows(JsonMappingException.class, () -> client.getZones());
    }

    @Test
    void getDnsRecordsWithNull() {
        assertThrows(JsonMappingException.class, () -> client.getDnsRecords(1000));
    }

    @Test
    void getDnsRecordsNot200() {
        HttpErrorException httpErrorException = assertThrows(HttpErrorException.class, () -> client.getDnsRecords(1001));
        assertEquals(400, httpErrorException.getHttpStatusCode());
        assertNotNull(httpErrorException.toString());
    }

    private static void stubGetZonesFailDeserialization() throws IOException {
        String unknownBody = """
                { "unknown_field": "unknown_value" }
                """;
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(unknownBody)));
    }

    private static void stubGetDnsRecordsWithNull() throws IOException {
        String nullBody = """
                {
                  "success": true,
                  "timestamp": "2025-12-29T12:29:09.458Z",
                  "requestId": "mocked-request-id-123456",
                  "data": null
                }
                """;
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/1000/records"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(nullBody)));
    }

    private static void stubGetDnsRecordsNot200() throws IOException {
        String nullBody = """
                {
                  "success": false
                }
                """;
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/1001/records"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(nullBody)));
    }

    private static String readFixture(String filename) throws IOException {
        Path file = RESPONSES_DIR.resolve(Objects.requireNonNull(filename, "filename"));
        return Files.readString(file, StandardCharsets.UTF_8);
    }
}
