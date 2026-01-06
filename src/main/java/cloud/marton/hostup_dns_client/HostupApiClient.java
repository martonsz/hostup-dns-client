package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.JsonMappingException;
import cloud.marton.hostup_dns_client.exceptions.RateLimitException;
import cloud.marton.hostup_dns_client.model.*;
import com.dslplatform.json.DslJson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;

public class HostupApiClient {

    private static final Logger LOGGER = Logger.getLogger(HostupApiClient.class.getName());
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final URI baseUri;
    private final HttpClient client;
    private final String apiKey;
    private final DslJson<Object> dslJson;
    private final int maxRetries;
    private final long firstBackoffMillis;

    public HostupApiClient(String apiKey, URI baseUri) {
        this(apiKey, baseUri, 6, 30_000L);
    }

    public HostupApiClient(String apiKey, URI baseUri, int maxRetries, long firstBackoffMillis) {
        Objects.requireNonNull(apiKey, "API key must not be null");
        Objects.requireNonNull(baseUri, "Base URI must not be null");
        this.baseUri = baseUri;
        this.apiKey = apiKey;
        this.maxRetries = maxRetries;
        this.firstBackoffMillis = firstBackoffMillis;
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(CONNECTION_TIMEOUT)
                .build();
        dslJson = cloud.marton.hostup_dns_client.json.DslJsonFactory.create();
    }

    /**
     * Retrieve a list of all DNS zones associated with a specific customer account. This is useful for getting an overview of your domains' DNS settings and for managing them.
     * <a href="https://developer.hostup.se/#tag/domain-services/GET/api/dns/zones">API Documentation</a>
     *
     * @return {@link ApiResponse} with {@link ZonesResponse}
     */
    public ApiResponse getZones() throws IOException, InterruptedException, RateLimitException, JsonMappingException {
        HttpRequest request = newRequestBuilder("dns/zones")
                .GET()
                .build();
        return send(request, ZonesResponse.class);
    }

    /**
     * Display all DNS records (such as A, CNAME, MX, NS) associated with a specific domain zone. Use this when you need to view, troubleshoot, or manage DNS settings for your domain. -
     * <a href="https://developer.hostup.se/#tag/domain-services/GET/api/dns/zones/{zoneId}/records">API Documentation</a>
     *
     * @return {@link ApiResponse} with {@link DnsRecordsResponse}
     */
    public ApiResponse getDnsRecords(int zoneId) throws IOException, InterruptedException, RateLimitException, JsonMappingException {
        HttpRequest request = newRequestBuilder("dns/zones/%d/records".formatted(zoneId))
                .GET()
                .build();
        return send(request, DnsRecordsResponse.class);
    }

    /**
     * Remove a specific DNS record, such as an A, CNAME, or MX record, associated with your domain. This is useful for cleaning up or correcting your domain's DNS settings.
     * <a href="https://developer.hostup.se/#tag/domain-services/DELETE/api/dns/zones/{zoneId}/records/{recordId}">API Documentation</a>
     *
     * @return {@link ApiResponse} with {@link DeleteDnsRecordResponse}
     */
    public ApiResponse deleteDnsRecord(int zoneId, int recordId) throws IOException, InterruptedException, RateLimitException, JsonMappingException {
        HttpRequest request = newRequestBuilder("dns/zones/%d/records/%d".formatted(zoneId, recordId))
                .DELETE()
                .build();
        return send(request, DeleteDnsRecordResponse.class);
    }

    /**
     * Calling {@link #setDnsRecord(int, String, String, String, int)  } with type="TXT" and ttl=300
     *
     * @return {@link ApiResponse} with {@link SetRecordResponse}
     */
    public ApiResponse setDnsRecord(int zoneId, String name, String value) throws IOException, InterruptedException, RateLimitException, JsonMappingException {
        return setDnsRecord(zoneId, "TXT", name, value, 300);
    }

    /**
     * Add, change, or delete DNS records (such as A, CNAME, MX, TXT) for your domain. This is necessary when configuring services like email, subdomains, or pointing your domain to another server.
     * <a href="https://developer.hostup.se/#tag/domain-services/POST/api/dns/zones/{zoneId}/records">API Documentation</a>
     *
     * @return {@link ApiResponse} with {@link ZonesResponse}
     */
    public ApiResponse setDnsRecord(int zoneId, String type, String name, String value, int ttl) throws IOException, InterruptedException, RateLimitException, JsonMappingException {
        String body = """
                {
                  "type": "%s",
                  "name": "%s",
                  "value": "%s",
                  "ttl": %d
                }
                """.formatted(type, name, value, ttl);
        HttpRequest request = newRequestBuilder("dns/zones/%d/records".formatted(zoneId))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json, charset=UTF-8")
                .build();
        return send(request, SetRecordResponse.class);
    }

    private <T extends HostupApiResponse> ApiResponse send(HttpRequest request, Class<T> responseType) throws RateLimitException, JsonMappingException, IOException, InterruptedException {
        return send(request, responseType, 0);
    }

    private <T extends HostupApiResponse> ApiResponse send(HttpRequest request, Class<T> responseType, int retryCount) throws IOException, InterruptedException, RateLimitException, JsonMappingException {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(responseType, "responseType");

        LOGGER.fine(() -> "Request  %s %s".formatted(request.method(), request.uri()));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int httpStatusCode = response.statusCode();

        String bodyAsString = response.body();
        LOGGER.fine(() -> "Response %s %s httpStatusCode: %d\n%s".formatted(request.method(), request.uri(), response.statusCode(), bodyAsString));
        if (httpStatusCode == 429) {
            if (retryCount >= maxRetries) {
                throw new RateLimitException(response.statusCode(), "Max retry (%d) attempts reached".formatted(maxRetries), bodyAsString);
            }
            long sleepDuration = calculateExponentialBackoff(retryCount + 1);
            LOGGER.warning(() -> "Rate limit reached (HTTP 429). Retry: %d/%d. Waiting %.1fs before trying again..."
                    .formatted(retryCount + 1, maxRetries, sleepDuration / 1000.0));
            Thread.sleep(sleepDuration);
            return send(request, responseType, retryCount + 1);
        }
        if (httpStatusCode != 200) {
            byte[] bodyBytes = bodyAsString.getBytes();
            try {
                ErrorResponse deserialized = dslJson.deserialize(ErrorResponse.class, bodyBytes, bodyBytes.length);
                return new ApiResponse(false, response.statusCode(), bodyAsString, deserialized);
            } catch (IOException e) {
                throw new JsonMappingException(httpStatusCode, "Failed to deserialize JSON as " + responseType.getName(), bodyAsString, e);
            }
        }
        byte[] bodyBytes = bodyAsString.getBytes();
        try {
            T deserialized = dslJson.deserialize(responseType, bodyBytes, bodyBytes.length);
            return new ApiResponse(true, response.statusCode(), bodyAsString, deserialized);
        } catch (IOException e) {
            throw new JsonMappingException(httpStatusCode, "Failed to deserialize JSON as " + responseType.getName(), bodyAsString, e);
        }
    }

    private long calculateExponentialBackoff(int retryCount) {
        int effectiveRetry = Math.max(1, retryCount);
        long baseDelayMillis = firstBackoffMillis * (1L << (effectiveRetry - 1));
        long jitter = (long) (Math.random() * 500L);
        return baseDelayMillis + jitter;
    }

    private HttpRequest.Builder newRequestBuilder(String relativePath) {
        URI target = baseUri.resolve(relativePath);
        return HttpRequest.newBuilder(target)
                .timeout(DEFAULT_TIMEOUT)
                .header("X-API-Key", apiKey);
    }
}
