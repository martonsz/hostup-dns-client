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
            @JsonAttribute(mandatory = true, nullable = false) String server_id,
            @JsonAttribute(mandatory = true, nullable = false) String account_id,
            @JsonAttribute(mandatory = true, nullable = false) String domain_id,
            @JsonAttribute(mandatory = true, nullable = false) String domain) {
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        for (Zone zone : data.zones()) {
            sb.append("Zone{\n");
            sb.append("  server_id='").append(zone.server_id()).append("',\n");
            sb.append("  account_id='").append(zone.account_id()).append("',\n");
            sb.append("  domain_id='").append(zone.domain_id()).append("',\n");
            sb.append("  domain='").append(zone.domain()).append("'\n");
            sb.append("}\n");
        }
        return sb.toString();
    }
}

