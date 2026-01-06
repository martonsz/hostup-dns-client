package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.JsonMappingException;
import cloud.marton.hostup_dns_client.exceptions.RateLimitException;
import cloud.marton.hostup_dns_client.model.DeleteDnsRecordResponse;
import cloud.marton.hostup_dns_client.model.DnsRecordsResponse;
import cloud.marton.hostup_dns_client.model.SetRecordResponse;
import cloud.marton.hostup_dns_client.model.ZonesResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HostupApiClientTest {

    private static final Path RESPONSES_DIR = Path.of("src", "test", "resources", "hostupApiResponses");
    private static WireMockServer wireMockServer;
    private static HostupApiClient client;

    @BeforeAll
    static void setup() throws URISyntaxException {
        cloud.marton.hostup_dns_client.logging.LoggingConfigurator.configure();

        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());


        URI baseUri = new URI(wireMockServer.baseUrl() + "/");
        client = new HostupApiClient("test-api-key", baseUri, 2, 10L);
//        client = new HostupApiClient(System.getenv("HOSTUP_DNS_CLIENT_API_KEY"), URI.create(CliParser.HOSTUP_DNS_CLIENT_BASE_URI));
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void getZones() throws RateLimitException, JsonMappingException, IOException, InterruptedException {
        stubGetZones();

        ZonesResponse zones = (ZonesResponse) client.getZones().parsedResponse();
        assertEquals("marton.cloud", zones.data().zones().getFirst().domain(), "First domain should match fixture");
        assertEquals("mock-domain.com", zones.data().zones().get(1).domain(), "First domain should match fixture");
    }

    @Test
    void getDnsRecords() throws RateLimitException, JsonMappingException, IOException, InterruptedException {
        stubGetDnsRecords();

        DnsRecordsResponse dnsRecords = (DnsRecordsResponse) client.getDnsRecords(10000).parsedResponse();
        assertEquals("www.marton.cloud", dnsRecords.data().zone().records().getFirst().name(), "First record name must match fixture");
        assertEquals("*.marton.cloud", dnsRecords.data().zone().records().get(1).name(), "First record name must match fixture");
        assertEquals("marton.cloud", dnsRecords.data().zone().records().get(2).name(), "First record name must match fixture");
    }

    @Test
    void setDnsRecord() throws RateLimitException, JsonMappingException, IOException, InterruptedException {
        stubSetDnsRecord();

        SetRecordResponse answer = (SetRecordResponse) client.setDnsRecord(10000, "foo.marton.cloud", "test-value").parsedResponse();
        assertEquals("foo.marton.cloud", answer.data().record().name(), "Record name must match fixture");
        assertEquals("\"test-value\"", answer.data().record().value(), "Record value must match fixture");
    }

    @Test
    void deleteDnsRecord() throws RateLimitException, JsonMappingException, IOException, InterruptedException {
        stubDeleteDnsRecord();

        DeleteDnsRecordResponse answer = (DeleteDnsRecordResponse) client.deleteDnsRecord(10000, 20000).parsedResponse();
        assertEquals("DNS record deleted successfully", answer.data().message(), "Deleted record ID must match fixture");
    }

    @Test
    void getDnsRecordsRateLimitReached() {
        stubGetDnsRecordsRateLimitReached();

        assertThrows(RateLimitException.class, () -> client.getDnsRecords(10001));
    }

    @Test
    void getDnsRecordsRateLimitRetry() throws RateLimitException, JsonMappingException, IOException, InterruptedException {
        stubGetDnsRecordsRateLimitRetry();

        client.getDnsRecords(10002);
    }

    private static void stubGetZones() throws IOException {
        String body = readFixture("listZones.json");
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private static void stubGetDnsRecords() throws IOException {
        String body = readFixture("getDnsRecords.json");
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/10000/records"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private static void stubGetDnsRecordsRateLimitReached() {
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/10001/records"))
                .willReturn(aResponse()
                        .withStatus(429)));
    }

    private static void stubGetDnsRecordsRateLimitRetry() throws IOException {
        String body = readFixture("getDnsRecords.json");
        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/10002/records"))
                .inScenario("Rate Limit Retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(429))
                .willSetStateTo("Retry 1"));

        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/10002/records"))
                .inScenario("Rate Limit Retry")
                .whenScenarioStateIs("Retry 1")
                .willReturn(aResponse().withStatus(429))
                .willSetStateTo("Retry 2"));

        wireMockServer.stubFor(get(urlPathEqualTo("/dns/zones/10002/records"))
                .inScenario("Rate Limit Retry")
                .whenScenarioStateIs("Retry 2")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private static void stubSetDnsRecord() {
        String body = """
                {
                  "success": true,
                  "timestamp": "2026-01-03T20:27:22.879Z",
                  "requestId": "mocked-request-id",
                  "data": {
                    "record": {
                      "id": 30000000,
                      "type": "TXT",
                      "name": "foo.marton.cloud",
                      "value": "\\"test-value\\"",
                      "ttl": 300,
                      "status": "pending"
                    }
                  }
                }
                """;
        wireMockServer.stubFor(post(urlPathEqualTo("/dns/zones/10000/records"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private static void stubDeleteDnsRecord() {
        String body = """
                {
                  "success": true,
                  "timestamp": "2026-01-03T20:27:22.879Z",
                  "requestId": "mocked-request-id",
                  "data": {
                    "message": "DNS record deleted successfully"
                  }
                }
                """;
        wireMockServer.stubFor(delete(urlPathEqualTo("/dns/zones/10000/records/20000"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private static String readFixture(String filename) throws IOException {
        Path file = RESPONSES_DIR.resolve(Objects.requireNonNull(filename, "filename"));
        return Files.readString(file, StandardCharsets.UTF_8);
    }
}
