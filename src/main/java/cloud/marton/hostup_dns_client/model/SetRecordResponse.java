package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson()
public record SetRecordResponse(
        @JsonAttribute(mandatory = true, nullable = false) boolean success,
        @JsonAttribute(mandatory = true, nullable = false) String requestId,
        @JsonAttribute(mandatory = true, nullable = false) Data data) implements HostupApiResponse {

    public String pretty() {
        return """
                SetRecordResponse:
                  success: %s
                  requestId: %s
                  data:
                    record:
                      id: %d
                      type: %s
                      name: %s
                      value: %s
                      ttl: %d
                      status: %s
                """.formatted(
                success,
                requestId,
                data.record.id,
                data.record.type,
                data.record.name,
                data.record.value,
                data.record.ttl,
                data.record.status
        );
    }

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
