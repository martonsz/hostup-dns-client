package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.JsonMappingException;
import cloud.marton.hostup_dns_client.exceptions.RateLimitException;
import cloud.marton.hostup_dns_client.model.ApiResponse;
import cloud.marton.hostup_dns_client.model.ErrorResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class HostupApiClientFailTest {

    private static final Path RESPONSES_DIR = Path.of("src", "test", "resources", "hostupApiResponses");
    private static WireMockServer wireMockServer;
    private static HostupApiClient client;

    @BeforeAll
    static void setup() throws URISyntaxException {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
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
        stubGetZonesFailDeserialization();

        assertThrows(JsonMappingException.class, () -> client.getZones());
    }

    @Test
    void getDnsRecordsWithNull() {
        stubGetDnsRecordsWithNull();

        assertThrows(JsonMappingException.class, () -> client.getDnsRecords(1000));
    }

    @Test
    void getDnsRecordsNot404() throws RateLimitException, JsonMappingException, IOException, InterruptedException {
        stubGetDnsRecords404();

        ApiResponse response = client.getDnsRecords(1001);
        assertEquals(404, response.httpStatus());
        assertEquals("Not Found", ((ErrorResponse) response.parsedResponse()).error());
        assertEquals("NOT_FOUND", ((ErrorResponse) response.parsedResponse()).code());
    }

    @Test
    void getDnsRecordsUnknownJson() {
        stubGetDnsRecordsUnknownJson();

        JsonMappingException httpErrorException = assertThrows(JsonMappingException.class, () -> client.getDnsRecords(1002));
        assertEquals(400, httpErrorException.getHttpStatusCode());
        assertNotNull(httpErrorException.toString());
    }

    private static void stubGetZonesFailDeserialization() {
        String unknownBody = """
                { "unknown_field": "unknown_value" }
                """;
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(unknownBody)));
    }

    private static void stubGetDnsRecordsWithNull() {
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

    private static void stubGetDnsRecords404() {
        String nullBody = """
                {
                  "error": "Not Found",
                  "message": "dns.zone_not_found not found",
                  "code": "NOT_FOUND",
                  "timestamp": "2026-01-04T09:09:03.358Z",
                  "requestId": "4a3236f3-7389-4806-9790-13503912ca8c"
                }
                """;
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/1001/records"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(nullBody)));
    }


    private static void stubGetDnsRecordsUnknownJson() {
        String nullBody = """
                {
                  "foo": "bar"
                }
                """;
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/1002/records"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(nullBody)));
    }
}
