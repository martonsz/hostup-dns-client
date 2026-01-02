package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson()
public record ZonesResponse(boolean success,
                            String requestId,
                            @JsonAttribute(mandatory = true, nullable = false) Data data) {

    public record Data(
            @JsonAttribute(mandatory = true, nullable = false) List<Zone> zones) {
    }

    public record Zone(String server_id,
                       String account_id,
                       String domain_id,
                       String domain) {
    }
}

