package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson()
public record SetRecordResponse(
        @JsonAttribute(mandatory = true, nullable = false) boolean success,
        @JsonAttribute(mandatory = true, nullable = false) String requestId,
        @JsonAttribute(mandatory = true, nullable = false) Data data) implements HostupApiResponse {

    public record Data(
            @JsonAttribute(mandatory = true, nullable = false) Record record) {
    }

    public record Record(
            @JsonAttribute(mandatory = true, nullable = false) int id,
            @JsonAttribute(mandatory = true, nullable = false) String type,
            @JsonAttribute(mandatory = true, nullable = false) String name,
            @JsonAttribute(mandatory = true, nullable = false) String value,
            @JsonAttribute(mandatory = true, nullable = false) int ttl,
            @JsonAttribute(mandatory = true, nullable = false) String status) {
    }
}
