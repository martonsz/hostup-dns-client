package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson()
public record ErrorResponse(
        @JsonAttribute(mandatory = true, nullable = false) String error,
        @JsonAttribute(mandatory = true, nullable = false) String message,
        @JsonAttribute(mandatory = true, nullable = false) String code,
        @JsonAttribute(mandatory = true, nullable = false) String timestamp,
        @JsonAttribute(mandatory = true, nullable = false) String requestId)
        implements HostupApiResponse {
}
