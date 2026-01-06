package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson()
public record DnsRecordsResponse(
        @JsonAttribute(mandatory = true, nullable = false) boolean success,
        @JsonAttribute(mandatory = true, nullable = false) String requestId,
        @JsonAttribute(mandatory = true, nullable = false) Data data) implements HostupApiResponse {

    public record Data(
            @JsonAttribute(mandatory = true, nullable = false) Zone zone) {
    }

    public record Zone(
            @JsonAttribute(mandatory = true, nullable = false) String id,
            @JsonAttribute(mandatory = true, nullable = false) String domain,
            @JsonAttribute(mandatory = true, nullable = false) List<Record> records) {
    }

    public record Record(
            @JsonAttribute(mandatory = true, nullable = false) int id,
            @JsonAttribute(mandatory = true, nullable = false) String type,
            @JsonAttribute(mandatory = true, nullable = false) String name,
            @JsonAttribute(mandatory = true, nullable = false) String value,
            @JsonAttribute(mandatory = true, nullable = false) int ttl,
            @JsonAttribute(mandatory = true, nullable = false) String status,
            @JsonAttribute(mandatory = true, nullable = false) String created) {
    }

    public String pretty() {
        return "DNS Records Response:\n" +
                "Success: " + success + "\n" +
                "Request ID: " + requestId + "\n" +
                "Zone ID: " + data.zone().id() + "\n" +
                "Domain: " + data.zone().domain() + "\n" +
                "Records:\n" +
                data.zone().records().stream()
                        .map(record -> "  - ID: " + record.id() +
                                ", Type: " + record.type() +
                                ", Name: " + record.name() +
                                ", Value: " + record.value() +
                                ", TTL: " + record.ttl() +
                                ", Status: " + record.status() +
                                ", Created: " + record.created())
                        .reduce("", (a, b) -> a + b + "\n");
    }
}
