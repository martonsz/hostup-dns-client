package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson()
public record DnsRecordsResponse(boolean success,
                                 String requestId,
                                 @JsonAttribute(mandatory = true, nullable = false) Data data) {

    public record Data(
            @JsonAttribute(mandatory = true, nullable = false) Zone zone) {
    }

    public record Zone(String id,
                       String domain,
                       @JsonAttribute(mandatory = true, nullable = false) List<Record> records) {
    }

    public record Record(int id,
                         String type,
                         String name,
                         String value,
                         int ttl,
                         String status,
                         String created) {
    }
}
