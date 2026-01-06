package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson()
public record ZonesResponse(
        @JsonAttribute(mandatory = true, nullable = false) boolean success,
        @JsonAttribute(mandatory = true, nullable = false) String requestId,
        @JsonAttribute(mandatory = true, nullable = false) Data data) implements HostupApiResponse {

    public record Data(
            @JsonAttribute(mandatory = true, nullable = false) List<Zone> zones) {
    }

    public record Zone(
            @JsonAttribute(mandatory = true, nullable = false) int server_id,
            @JsonAttribute(mandatory = true, nullable = false) int account_id,
            @JsonAttribute(mandatory = true, nullable = false) int domain_id,
            @JsonAttribute(mandatory = true, nullable = false) String domain) {
    }
}

