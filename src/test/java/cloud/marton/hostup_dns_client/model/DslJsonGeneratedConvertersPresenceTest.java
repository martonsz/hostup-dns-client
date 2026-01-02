package cloud.marton.hostup_dns_client.model;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This test guards against accidentally packaging/running without DSL-JSON generated converters.
 * <p>
 * We verify two things:
 * 1) The generated converter classes exist on the classpath.
 * 2) DSL-JSON can resolve readers/writers for our model types via tryFindReader/tryFindWriter,
 * which strongly indicates converters were registered (rather than failing and relying on
 * some other/incorrect mapping setup).
 */
class DslJsonGeneratedConvertersPresenceTest {

    private static final DslJson<Object> DSL_JSON = new DslJson<>();

    @Test
    void generatedConvertersArePresentOnClasspath_andReadersWritersAreRegistered() {
        // Presence check (annotation processing output included)
        assertDoesNotThrow(() -> Class.forName(
                "cloud.marton.hostup_dns_client.model._ZonesResponse_DslJsonConverter"));
        assertDoesNotThrow(() -> Class.forName(
                "cloud.marton.hostup_dns_client.model._ZonesResponse$Data_DslJsonConverter"));
        assertDoesNotThrow(() -> Class.forName(
                "cloud.marton.hostup_dns_client.model._ZonesResponse$Zone_DslJsonConverter"));

        assertDoesNotThrow(() -> Class.forName(
                "cloud.marton.hostup_dns_client.model._DnsRecordsResponse_DslJsonConverter"));
        assertDoesNotThrow(() -> Class.forName(
                "cloud.marton.hostup_dns_client.model._DnsRecordsResponse$Data_DslJsonConverter"));
        assertDoesNotThrow(() -> Class.forName(
                "cloud.marton.hostup_dns_client.model._DnsRecordsResponse$Zone_DslJsonConverter"));
        assertDoesNotThrow(() -> Class.forName(
                "cloud.marton.hostup_dns_client.model._DnsRecordsResponse$Record_DslJsonConverter"));

        // Registration check (DSL-JSON can resolve converters)
        JsonReader.ReadObject<ZonesResponse> zonesReader = DSL_JSON.tryFindReader(ZonesResponse.class);
        JsonWriter.WriteObject<ZonesResponse> zonesWriter = DSL_JSON.tryFindWriter(ZonesResponse.class);
        assertNotNull(zonesReader, "No reader registered for ZonesResponse (generated converter not picked up?)");
        assertNotNull(zonesWriter, "No writer registered for ZonesResponse (generated converter not picked up?)");

        JsonReader.ReadObject<DnsRecordsResponse> dnsReader = DSL_JSON.tryFindReader(DnsRecordsResponse.class);
        JsonWriter.WriteObject<DnsRecordsResponse> dnsWriter = DSL_JSON.tryFindWriter(DnsRecordsResponse.class);
        assertNotNull(dnsReader, "No reader registered for DnsRecordsResponse (generated converter not picked up?)");
        assertNotNull(dnsWriter, "No writer registered for DnsRecordsResponse (generated converter not picked up?)");
    }
}
