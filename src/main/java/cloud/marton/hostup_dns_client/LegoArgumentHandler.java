package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.JsonMappingException;
import cloud.marton.hostup_dns_client.exceptions.LegoArgumentResult;
import cloud.marton.hostup_dns_client.exceptions.RateLimitException;
import cloud.marton.hostup_dns_client.model.ApiResponse;
import cloud.marton.hostup_dns_client.model.DnsRecordsResponse;
import cloud.marton.hostup_dns_client.model.ZonesResponse;

import java.io.IOException;
import java.util.Optional;

public class LegoArgumentHandler {
    private final HostupApiClient client;

    public LegoArgumentHandler(HostupApiClient client) {
        this.client = client;
    }

    public LegoArgumentResult handleLegoArgs(CliParser.LegoArgs legoArgs) throws RateLimitException,
            JsonMappingException,
            IOException,
            InterruptedException {
        ApiResponse zones = client.getZones();
        if (!zones.success()) {
            return new LegoArgumentResult(false, "Could not get zones", zones);
        }
        String nakedDomain = getNakedDomain(legoArgs.domain());
        Optional<ZonesResponse.Zone> zone = ((ZonesResponse) zones.parsedResponse())
                .data()
                .zones()
                .stream()
                .filter(z -> z.domain().equalsIgnoreCase(nakedDomain))
                .findFirst();
        if (zone.isEmpty()) {
            return new LegoArgumentResult(false, "Could not find naked domain: " + nakedDomain, zones);
        }

        if (legoArgs.action() == CliParser.LegoAction.PRESENT) {
            ApiResponse apiResponse = client.setDnsRecord(zone.get().domain_id(), "TXT", legoArgs.domain(), legoArgs.value(), 300);
            if (!apiResponse.success()) {
                return new LegoArgumentResult(false, "Could not add TXT record for domain: " + legoArgs.domain(), apiResponse);
            }
            return new LegoArgumentResult(true, "Successfully added TXT record", apiResponse);
        } else {
            ApiResponse dnsRecords = client.getDnsRecords(zone.get().domain_id());
            if (!dnsRecords.success()) {
                return new LegoArgumentResult(false, "Could not get DNS records for domainId: " + zone.get().domain_id(), dnsRecords);
            }
            Optional<DnsRecordsResponse.Record> record = ((DnsRecordsResponse) dnsRecords.parsedResponse())
                    .data()
                    .zone()
                    .records()
                    .stream()
                    .filter(r -> r.name().equalsIgnoreCase(legoArgs.domain()))
                    .findFirst();
            if (record.isEmpty()) {
                return new LegoArgumentResult(false, "Could not find DNS record for domain: " + legoArgs.domain(), dnsRecords);
            }
            ApiResponse apiResponse = client.deleteDnsRecord(zone.get().domain_id(), record.get().id());
            if (!apiResponse.success()) {
                return new LegoArgumentResult(false, "Could not delete TXT record for domain: " + legoArgs.domain(), apiResponse);
            }
            return new LegoArgumentResult(true, "Successfully deleted DNS record", apiResponse);
        }
    }

    private String getNakedDomain(String domain) {
        String[] parts = domain.split("\\.");
        if (parts.length < 2) {
            return domain;
        } else {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        }
    }


}
