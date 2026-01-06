package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.CliParserException;
import cloud.marton.hostup_dns_client.exceptions.JsonMappingException;
import cloud.marton.hostup_dns_client.exceptions.RateLimitException;
import cloud.marton.hostup_dns_client.logging.LoggingConfigurator;
import cloud.marton.hostup_dns_client.model.ApiResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    static {
        LoggingConfigurator.configure();
    }

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    static void main(String[] args) {
        try {
            CliParser cliParser = new CliParser();
            CliParser.CliOptions options = cliParser.parseArgs(args);

            if (options.help()) {
                System.out.println("Usage:\n" + CliParser.getUsage());
                return;
            }
            if (options.version()) {
                System.out.println(getVersion());
                return;
            }
            HostupApiClient client = new HostupApiClient(options.apiKey(), options.baseUri());
            if (options.addRecord() != null) {
                var record = options.addRecord();
                ApiResponse apiResponse = client.setDnsRecord(
                        record.zoneId(),
                        record.type(),
                        record.domain(),
                        record.value(),
                        record.ttl());
                printResponse(apiResponse);
            } else if (options.deleteDomain() != null) {
                throw new RuntimeException("Not implemented yet");
            } else if (options.deleteRecord() != null) {
                ApiResponse apiResponse = client.deleteDnsRecord(options.deleteRecord().zoneId(), options.deleteRecord().recordId());
                printResponse(apiResponse);
            } else if (options.listZones()) {
                printResponse(client.getZones());
            } else if (options.listRecords() != null) {
                printResponse(client.getDnsRecords(options.listRecords()));
            } else if (options.legoArgs() != null) {
                throw new RuntimeException("Not implemented yet");
            } else {
                throw new CliParserException("I forgot to implement this option\nArgs:\n"
                        + Arrays.toString(args) + "\n"
                        + CliParser.CliOptions.class.getName() + ":\n"
                        + options);
            }
        } catch (CliParserException e) {
            LOGGER.log(Level.SEVERE, e, () -> e.getMessage() + "\n\n" + CliParser.getUsage());
            System.exit(1);
        } catch (IOException | InterruptedException | RateLimitException |
                 JsonMappingException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            System.exit(1);
        }
    }

    private static void printResponse(ApiResponse apiResponse) {
        if (apiResponse.success()) {
            System.out.println(apiResponse.body());
        } else {
            System.err.println(apiResponse.body());
            System.exit(1);
        }
    }

    private static String getVersion() {
        return Main.class.getPackage().getImplementationVersion();
    }

}
