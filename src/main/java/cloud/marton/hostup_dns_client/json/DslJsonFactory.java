package cloud.marton.hostup_dns_client.json;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;

/**
 * Factory class to create and configure DslJson instances with generated converters.
 * This is so that GraalVM native images find all the necessary classes at build time.
 */
public final class DslJsonFactory {

    private DslJsonFactory() {
    }

    public static DslJson<Object> create() {

        // Create DslJson with runtime Settings (works for generated converters)
        DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime());

        // Explicitly register generated converters into this instance
        new cloud.marton.hostup_dns_client.model._DeleteDnsRecord404Response_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._DeleteDnsRecordResponse$Data_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._DeleteDnsRecordResponse_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._DnsRecordsResponse$Data_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._DnsRecordsResponse$Record_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._DnsRecordsResponse$Zone_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._DnsRecordsResponse_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._ErrorResponse_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._SetRecordResponse$Data_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._SetRecordResponse$Record_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._SetRecordResponse_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._ZonesResponse$Data_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._ZonesResponse$Zone_DslJsonConverter().configure(dslJson);
        new cloud.marton.hostup_dns_client.model._ZonesResponse_DslJsonConverter().configure(dslJson);

        return dslJson;
    }
}
